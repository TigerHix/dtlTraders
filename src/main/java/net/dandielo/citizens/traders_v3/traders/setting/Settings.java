package net.dandielo.citizens.traders_v3.traders.setting;

import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.trait.trait.Owner;
import net.citizensnpcs.api.util.DataKey;
import net.dandielo.citizens.traders_v3.core.Debugger;

public class Settings extends TGlobalSettings {	
    //the Npc associated with these settings
	private final NPC npc;
	private String owner;
	private String type = "server"; //needs to be here when some1 will apply it with the /trait command
	
	//npc related settings
	private int stockSize = TGlobalSettings.stockSize;
	private String stockNameFormat = TGlobalSettings.stockNameFormat;
	private String stockStart = TGlobalSettings.stockStart;
	
	public Settings(NPC npc) {
		this.npc = npc;
	}
	
	public NPC getNPC()
	{
		return npc;
	}
	
	//trader type
	public String getType()
	{
		return type;
	}
	
	/**
	 * Should be used only when creating a new NPC and setting default values. 
	 * Sets the traders type
	 */
	public void setType(String type)
	{
		this.type = type;
	}
	
	//trader owner
	public String getOwner()
	{
		return owner;
	}
	public void setOwner(String owner)
	{
		this.owner = owner;
	}
	
	//npc owner
	public String getNpcOwner()
	{
		return npc.getTrait(Owner.class).getOwner();
	}
	
	//stock size
	public int getStockSize() {
		return stockSize;
	}
	
	public String getStockName() {
		return stockNameFormat.replace("{npc}", npc.getName());
	}
	
	public String getStockStart()
	{
		return stockStart;
	}
	
	public String getManagerStockStart()
	{
		return mmStockStart;
	}

	//loading and saving
	public void load(DataKey data) 
	{
		//debug info
		Debugger.info("Loading trader settings for: ", this.npc.getName());
		
		//load trader settings
		type = data.getString("type");
		owner = data.getString("owner", "no owner");
		
		//load stock settings
		stockSize = data.getInt("stock.size", TGlobalSettings.stockSize);
		stockNameFormat = data.getString("stock.format", TGlobalSettings.stockNameFormat);
		stockStart = data.getString("stock.default", TGlobalSettings.stockStart);
	}
	
	public void save(DataKey data) 
	{
		//debug info
		Debugger.info("Saving trader settings for:", this.npc.getName());
		
		//save trader settings
		data.setString("type", type);
		data.setString("owner", owner);
		data.setRaw("stock", null);
		
		//save stock settings
		if ( stockSize != TGlobalSettings.stockSize )
			data.setInt("stock.size", stockSize);
		if ( !stockNameFormat.equals(TGlobalSettings.stockNameFormat) )
			data.setString("stock.format", stockNameFormat);
		if ( !stockStart.equals(TGlobalSettings.stockStart) )
			data.setString("stock.default", stockStart);
			
	}

}
