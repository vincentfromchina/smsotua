package com.dayu.autosms.dummy;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Helper class for providing sample content for user interfaces created by
 * Android template wizards.
 * <p>
 * TODO: Replace all uses of this class before publishing your app.
 */
public class DummyContent
{

	/**
	 * An array of sample (dummy) items.
	 */
	public static List<DummyItem> ITEMS = new ArrayList<DummyItem>();

	/**
	 * A map of sample (dummy) items, by ID.
	 */
	public static Map<String, DummyItem> ITEM_MAP = new HashMap<String, DummyItem>();

	static
	{
		// Add 3 sample items.
		addItem(new DummyItem("1", "净利润最多"));
		addItem(new DummyItem("2", "佣金最多"));
		addItem(new DummyItem("3", "分红最多"));
		addItem(new DummyItem("4", "交易金额最大"));
		addItem(new DummyItem("5", "交易次数最多"));
		addItem(new DummyItem("6", "盈利百分比最高"));
		addItem(new DummyItem("7", "百分比最高(+分红-佣金)"));
	}

	private static void addItem(DummyItem item)
	{
		ITEMS.add(item);
		ITEM_MAP.put(item.id, item);
	}

	/**
	 * A dummy item representing a piece of content.
	 */
	public static class DummyItem
	{
		public String id;
		public String content;

		public DummyItem(String id, String content)
		{
			this.id = id;
			this.content = content;
		}

		@Override
		public String toString()
		{
			return content;
		}
	}
}
