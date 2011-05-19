package edu.stanford.socialflows.sticker;

import java.util.Iterator;
import java.util.Vector;

public class StickerIcons 
{
	private static StickerIcons instance = new StickerIcons();
	private Vector<StickerIcon> stickerCollection = new Vector<StickerIcon>();
	
	protected StickerIcons()
	{
		initialize();
	}
	
	private void initialize()
	{
		addStickerIcon(new StickerIcon("Dinosaur", "stickers_icons/sticker_dinosaur.png"));
		addStickerIcon(new StickerIcon("Favorites", "stickers_icons/sticker_favorites.png"));
		addStickerIcon(new StickerIcon("Flash", "stickers_icons/sticker_flash.png"));
		addStickerIcon(new StickerIcon("Plane", "stickers_icons/sticker_plane.png"));
		addStickerIcon(new StickerIcon("Garage Band", "stickers_icons/sticker_garageband.png"));
		addStickerIcon(new StickerIcon("Home", "stickers_icons/sticker_home.png"));
		addStickerIcon(new StickerIcon("Music", "stickers_icons/sticker_music.png"));
		addStickerIcon(new StickerIcon("World", "stickers_icons/sticker_world.png"));
		addStickerIcon(new StickerIcon("Pirate", "stickers_icons/sticker_pirate.png"));
		addStickerIcon(new StickerIcon("Robot", "stickers_icons/sticker_robot.png"));
		
		addStickerIcon(new StickerIcon("Evil Pumpkin", "stickers_icons/sticker_evil_pumpkin.png"));
		addStickerIcon(new StickerIcon("Fox", "stickers_icons/sticker_fox.png"));
		addStickerIcon(new StickerIcon("Key", "stickers_icons/sticker_key.png"));
		addStickerIcon(new StickerIcon("Leaf", "stickers_icons/sticker_leaf.png"));
		addStickerIcon(new StickerIcon("Ninja", "stickers_icons/sticker_ninja.png"));
		addStickerIcon(new StickerIcon("Paint", "stickers_icons/sticker_paint.png"));
		addStickerIcon(new StickerIcon("Pumpkin", "stickers_icons/sticker_pumpkin.png"));
		addStickerIcon(new StickerIcon("Shopping", "stickers_icons/sticker_shopping.png"));
		addStickerIcon(new StickerIcon("Skull", "stickers_icons/sticker_skull.png"));
		
		addStickerIcon(new StickerIcon("PrPl", "stickers_icons/prpl_icon.png"));
	}
	
	protected void addStickerIcon(StickerIcon icon)
	{
		stickerCollection.add(icon);
	}
	
	public static Iterator<StickerIcon> getStickerCollection()
	{
		return instance.stickerCollection.iterator();
	}
	
	public static int getStickerCollectionSize()
	{
		return instance.stickerCollection.size();
	}
		
	public class StickerIcon {
		String stickerIconURL = null;
		String stickerCaption = null;
		
		public StickerIcon(String caption, String url)
		{
			this.stickerIconURL = url;
			this.stickerCaption = caption;
		}
		
		public String getIconURL()
		{	return this.stickerIconURL;		}
		
		public String getCaption()
		{	return this.stickerCaption;		}
	}
}
