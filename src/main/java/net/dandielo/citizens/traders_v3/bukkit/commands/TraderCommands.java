package net.dandielo.citizens.traders_v3.bukkit.commands;

import java.util.Map;

import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.trait.trait.MobType;
import net.dandielo.citizens.traders_v3.tNpcManager;
import net.dandielo.citizens.traders_v3.bukkit.DtlTraders;
import net.dandielo.citizens.traders_v3.core.commands.Command;
import net.dandielo.citizens.traders_v3.core.exceptions.InvalidTraderTypeException;
import net.dandielo.citizens.traders_v3.core.exceptions.TraderTypeNotFoundException;
import net.dandielo.citizens.traders_v3.core.locale.LocaleManager;
import net.dandielo.citizens.traders_v3.traders.Trader;
import net.dandielo.citizens.traders_v3.traders.Trader.Status;
import net.dandielo.citizens.traders_v3.traits.TraderTrait;
import net.dandielo.citizens.traders_v3.traits.WalletTrait;

public class TraderCommands {

	private static LocaleManager locale = LocaleManager.locale;

	@Command(
	name = "trader",
	syntax = "create {args}",
	perm = "dtl.trader.commands.create",
	desc = "creates a new trader with the given arguments | 'e:', 't:'",
	usage = "- /trader create Wool trader e:sheep t:server",
	npc = false)
	public void traderCreate(DtlTraders plugin, Player sender, Trader trader, Map<String, String> args) throws TraderTypeNotFoundException, InvalidTraderTypeException
	{
		//get the traders name
		String name = args.get("free");
		
		//get the entity
		EntityType entity = EntityType.fromName(args.get("e") == null ? "player" : args.get("e"));

		//the the type
		String type = args.get("t") == null ? "server" : args.get("t");
		
		if ( name == null )
		{
			locale.sendMessage(sender, "error-argument-missing");
			return;
		}
		
		//Create the npc
		NPC npc = CitizensAPI.getNPCRegistry().createNPC(entity, name);
		
		//add Traits
		npc.addTrait(TraderTrait.class);
		npc.addTrait(WalletTrait.class);
		
		//set the mob type
		npc.addTrait(MobType.class);
		npc.getTrait(MobType.class).setType(entity);

		//spawn and the location of the sender
		npc.spawn(sender.getLocation());

		//create a test Trader Npc
		Trader nTrader = (Trader) tNpcManager.create_tNpc(npc, type, sender);
		nTrader.getSettings().setType(type);
		
		//start with the unlocked status, to allow fast stock setting 
		nTrader.parseStatus(Status.MANAGE_UNLOCKED);
		
		//register the relation
		tNpcManager.instance().registerRelation(sender, nTrader);
		
		//send message
		locale.sendMessage(sender, "trader-created", "player", sender.getName(), "trader", name);
	}
	/*
	@Command(
	name = "trader",
	syntax = "hire {args}",
	perm = "dtl.trader.commands.hire",
	desc = "creates a new player trader ready for use | for players",
	usage = "- /trader hire My Trader",
	npc = false)
	public void traderHire(DtlTraders plugin, Player sender, Trader trader, Map<String, String> args)
	{
		if ( !TraderTrait.addTrader(sender.getName()) )
		{
			locale.sendMessage(sender, "trader-hire-limit");
			return;
		}
		
		String name = args.get("free");
		WalletType wallet = WalletType.getTypeByName(args.get("w") == null ? "npc" : args.get("w"));

		if ( name == null )
		{
			locale.sendMessage(sender, "error-argument-missing");
			return;
		}
		if ( wallet == null )
			wallet = WalletType.NPC;
		
		// Create and spawn the npc
		NPC npc = CitizensAPI.getNPCRegistry().createNPC(EntityType.PLAYER, name);
		npc.addTrait(TraderTrait.class);
		npc.spawn(sender.getLocation());
		
		// Add basic settings
		npc.getTrait(TraderTrait.class).setType(EType.PLAYER_TRADER);
		npc.getTrait(TraderTrait.class).implementTrader();

		TraderConfigPart settings = npc.getTrait(TraderTrait.class).getConfig();
		settings.setOwner(sender.getName());
		settings.getWallet().setType(wallet);
		
		locale.sendMessage(sender, "trader-created", "player", sender.getName(), "trader", name);
	}
	
	@Command(
	name = "trader",
	syntax = "manage {args}",
	desc = "toggle manager mode for a trader",
	usage = "- /trader manage Wool trader",
	perm = "dtl.trader.commands.manage",
	npc = false)
	public void traderManage(DtlTraders plugin, Player sender, Trader trader, Map<String, String> args)
	{
		NpcManager man = DtlTraders.getNpcEcoManager();
		String name = args.get("free");

		if ( name == null )
		{
			if ( trader != null )
			{
				locale.sendMessage(sender, "managermode-disabled", "npc", trader.getNpc().getName());
				man.removeInteractionNpc(sender.getName());
			}
			return;
		}
		else
		{
			String player = sender.getName();
			
			if ( trader != null )
			{
				locale.sendMessage(sender, "managermode-disabled", "npc", trader.getNpc().getName());
				man.removeInteractionNpc(player);
			}
				
			trader = man.traderByName(name, sender);
			man.addInteractionNpc(player, trader);
			
			locale.sendMessage(sender, "managermode-enabled", "npc", man.getInteractionNpc(player).getNpc().getName());
		}
	}
	
	@Command(
	name = "trader",
	syntax = "open",
	desc = "opens the managed traders stock form any place",
	perm = "dtl.trader.commands.open")
	public void traderOpen(DtlTraders plugin, Player player, Trader trader, Map<String, String> args)
	{
		MetaTools.removeDescriptions(player.getInventory());
		if ( !trader.getTraderStatus().isManaging() )
			trader.loadDescriptions(player, player.getInventory());	
		
		player.openInventory(trader.getInventory());
	}
	
	//TODO Log commands
	@Command(
	name = "trader",
	syntax = "log <task>",
	desc = "shows or clears the log for the selected trader or for all traders if no one is selected",
	usage = "- /trader log show",
	perm = "dtl.trader.commands.log",
	npc = false)
	public void log(DtlTraders plugin, CommandSender sender, Trader npc, Map<String, String> args)
	{
		LogManager log = DtlTraders.getLoggingManager();
		
		if ( args.get("task").equals("show") )
		{
			List<String> logs = log.getPlayerLogs(sender.getName(), npc == null ? "" : npc.getNpc().getName());
			
			if ( logs == null )	return;
			
			for ( String entry : logs )
				sender.sendMessage(entry);
		}
		if ( args.get("task").equals("clear") )
		{
			log.clearPlayerLogs(sender.getName(), npc == null ? "" : npc.getNpc().getName());
			locale.sendMessage(sender, "trader-log-cleared");
		}
	}
	
	
	//TODO Config commands
	@Command(
	name = "trader",
	syntax = "reset <target> (stock)",
	desc = "clears the stock or resets prices, given stock or both if 'stock' argument is empty",
	usage = "- /trader reset buy",
	perm = "dtl.trader.commands.owner")
	public void tradeReset(DtlTraders plugin, CommandSender sender, Trader npc, Map<String, String> args)
	{		
		String target = args.get("target");
		
		if ( !target.equals("prices") && !target.equals("stock") )
		{
			locale.sendMessage(sender, "error-argument-invalid", "argument", target);
			return;
		}
		
		if ( args.containsKey("stock") )
		{
			String stock = args.get("stock");
			if ( stock.equals("sell") )
			{
				npc.getStock().reset(stock, target);
				
				locale.sendMessage(sender, "trader-stock-cleared", "stock", "#sell-stock");
			}
			else
			if ( stock.equals("buy") )
			{
				npc.getStock().reset(stock, target);

				locale.sendMessage(sender, "trader-stock-cleared", "stock", "#buy-stock");
			}
			else
				locale.sendMessage(sender, "error-argument-invalid", "argument", stock);
		}

		npc.getStock().reset(null, target);
		
		locale.sendMessage(sender, "trader-stock-cleared", "stock", "#sell-stock");
		locale.sendMessage(sender, "trader-stock-cleared", "stock", "#buy-stock");
	}
	
	@Command(
	name = "trader",
	syntax = "owner",
	desc = "shows the traders owner, it's not same as the Npc owner",
	perm = "dtl.trader.commands.owner")
	public void tradeOwner(DtlTraders plugin, CommandSender sender, Trader npc, Map<String, String> args)
	{
		locale.sendMessage(sender, "key-value", "key", "#owner", "value", npc.getConfig().getOwner());
	}
	
	@Command(
	name = "trader",
	syntax = "owner set <player>",
	desc = "changes the traders owner",
	usage = "- /trader owner set dandielo",
	perm = "dtl.trader.commands.owner.set")
	public void tradeSetOwner(DtlTraders plugin, CommandSender sender, Trader npc, Map<String, String> args)
	{
		npc.getConfig().setOwner(args.get("player"));
		locale.sendMessage(sender, "key-change", "key", "#owner", "value", args.get("player"));
	}
	
	//TODO pattern commands
	@Command(
	name = "trader",
	syntax = "pattern",
	desc = "shows the pattern set for the selected trader or 'disabled' otherwise",
	perm = "dtl.trader.commands.pattern")
	public void tradePattern(DtlTraders plugin, CommandSender sender, Trader npc, Map<String, String> args)
	{
		if ( !npc.getBase().getStock().getPatterns().isEmpty() )
		{
			locale.sendMessage(sender, "key-value", "key", "#pattern", "value", "");
			int i = 1;
			for ( Map.Entry<Integer, TPattern> pat : npc.getBase().getStock().getPatterns().entrySet() )
			{
				locale.sendMessage(sender, "key-value", "key", "" + i++, "value", pat.getValue().getName() + " (" + ChatColor.GREEN + pat.getValue().getType() + ChatColor.AQUA + ") - " + pat.getKey());
			}
		}
		else
			locale.sendMessage(sender, "key-value", "key", "#pattern", "value", "#disabled");
	}
	
	//TODO pattern added message
	@Command(
	name = "trader",
	syntax = "pattern add <pattern> (priority)",
	desc = "adds a new pattern to a traders stock",
	usage = "- /trader pattern add global_prices 1",
	perm = "dtl.trader.commands.pattern")
	public void tradePatternAdd(DtlTraders plugin, CommandSender sender, Trader npc, Map<String, String> args)
	{
		String pattern = args.get("pattern");

		if ( !npc.getBase().getStock().addPattern(pattern, args.get("priority") == null ? 1 : Integer.parseInt(args.get("priority")) ) )
			locale.sendMessage(sender, "error-argument-invalid", "argument", pattern);
		else
			locale.sendMessage(sender, "key-change", "key", "#pattern", "value", pattern);
	}

	@Command(
	name = "trader",
	syntax = "pattern save <pattern> (arg) (post)",
	desc = "saves/creates a new pattern using the selected traders stock (stock/prices/all) | you can clear/reset the stock after it's saved",
	usage = "- /trader pattern save stock clear",
	perm = "dtl.trader.commands.pattern")
	public void tradePatternSave(DtlTraders plugin, CommandSender sender, Trader npc, Map<String, String> args)
	{
		PatternsManager man = DtlTraders.getPatternsManager();
		
		String pattern = args.get("pattern");

		if ( man.getPattern(pattern) != null )
		{
			locale.sendMessage(sender, "pattern-save-fail-exists", "pattern", pattern);
			return;
		}
		
		String arg = args.get("arg") == null ? "all" : args.get("arg");
		String post = args.get("post") == null ? "" : args.get("post");
		
		TraderStockPart stock = npc.getStock();
		
		man.setFromList(pattern, 
				stock.getStock("sell"), 
				stock.getStock("buy"), 
				arg);
		
		// reload patterns
		tradePatternReload(plugin, sender, npc, args);
		
		if ( post.equals("clear") )
			stock.reset(null, "stock");
			
		if ( post.equals("reset") )
			stock.reset(null, "prices");

		locale.sendMessage(sender, "pattern-save-success", "pattern", pattern);
	}
	
	@Command(
	name = "trader",
	syntax = "pattern remove <pattern>",
	desc = "removes pattern from a trader",
	perm = "dtl.trader.commands.pattern")
	public void tradePatternRemove(DtlTraders plugin, CommandSender sender, Trader trader, Map<String, String> args)
	{
		trader.getBase().getStock().removePattern(args.get("pattern"));
		locale.sendMessage(sender, "key-change", "key", "#pattern", "value", "#disabled");
	}
	
	@Command(
	name = "trader",
	syntax = "pattern reload",
	desc = "reloads all patterns and trader stocks",
	perm = "dtl.trader.commands.pattern",
	npc = false)
	public void tradePatternReload(DtlTraders plugin, CommandSender sender, Trader trader, Map<String, String> args)
	{
		// reload patterns
		DtlTraders.getPatternsManager().reload();
	}
	
	private static NumberFormat format = NumberFormat.getCurrencyInstance();
	//private static DecimalFormat format = new DecimalFormat("#.##");
	
	@Command(
	name = "trader",
	syntax = "wallet",
	desc = "shows the current wallet type",
	perm = "dtl.trader.commands.wallet")
	public void traderWallet(DtlTraders plugin, CommandSender sender, Trader npc, Map<String, String> args)
	{
		locale.sendMessage(sender, "key-value", "key", "#wallet", "value", npc.getWallet().getType().toString());
	}
	
	@Command(
	name = "trader",
	syntax = "wallet set <wallet>",
	desc = "sets a new wallet type",
	usage = "- /trader wallet set player",
	perm = "dtl.trader.commands.wallet")
	public void traderSetWallet(DtlTraders plugin, CommandSender sender, Trader npc, Map<String, String> args)
	{
		WalletType type = WalletType.getTypeByName(args.get("wallet"));
		if ( type == null )
		{
			locale.sendMessage(sender, "error-argument-invalid", "argument", args.get("wallet"));
			return;
		}
		
		npc.getWallet().setType(type);
		locale.sendMessage(sender, "key-change", "key", "#wallet", "value", type.toString());
	}
	
	@Command(
	name = "trader",
	syntax = "wallet deposit <amount>",
	desc = "deposits money to the 'npc' wallet",
	usage = "- /trader wallet deopsit 10",
	perm = "dtl.trader.commands.wallet")
	public void traderWalletDeposit(DtlTraders plugin, CommandSender sender, Trader npc, Map<String, String> args)
	{
		Wallet wallet = npc.getWallet();
		double amount = Double.parseDouble(args.get("amount"));
		
		if ( DtlTraders.getEconomy().withdrawPlayer(sender.getName(), amount).transactionSuccess() )
		{
			wallet.deposit(null, amount);
			locale.sendMessage(sender, "wallet-deposit", "amount", format.format(amount).replace("$", ""));
			locale.sendMessage(sender, "wallet-balance", "amount", format.format(wallet.getMoney()).replace("$", ""));
		}
		else
			locale.sendMessage(sender, "wallet-deposit-fail");
	}

	@Command(
	name = "trader",
	syntax = "wallet withdraw <amount>",
	desc = "withdraws money from the 'npc' wallet",
	usage = "- /trader wallet withdraw 10",
	perm = "dtl.trader.commands.wallet")
	public void traderWalletWithdraw(DtlTraders plugin, CommandSender sender, Trader npc, Map<String, String> args)
	{
		Wallet wallet = npc.getWallet();
		double amount = Double.parseDouble(args.get("amount"));
		
		if ( wallet.withdraw(null, amount) )
		{
			DtlTraders.getEconomy().depositPlayer(sender.getName(), amount);
			locale.sendMessage(sender, "wallet-withdraw", "amount", format.format(amount).replace("$", ""));
			locale.sendMessage(sender, "wallet-balance", "amount", format.format(wallet.getMoney()).replace("$", ""));
		}
		else
			locale.sendMessage(sender, "wallet-withdraw-fail");
	}

	@Command(
	name = "trader",
	syntax = "wallet balance",
	desc = "shows the 'npc' wallet balance",
	perm = "dtl.trader.commands.wallet")
	public void traderWalletBalance(DtlTraders plugin, CommandSender sender, Trader npc, Map<String, String> args)
	{
		Wallet wallet = npc.getWallet();
		if ( !wallet.getType().equals(WalletType.NPC) )
			locale.sendMessage(sender, "wallet-invalid");
		else
			locale.sendMessage(sender, "wallet-balance", "amount", format.format(wallet.getMoney()).replace("$", ""));
	}
	*/
}
