package de.blablubbabc.billboards.message;

import org.jetbrains.annotations.Nullable;

public enum Message {

	UNKNOWN_NAME("未知",
			"未知玩家名时显示的值"),
	UNKNOWN_UUID("-",
			"未知UUID时显示的值"),
	SERVER_OWNER_NAME("*系统*",
			"创建者为系统时显示的值"),
	YOU_HAVE_TO_SNEAK("&7你需要按住 Shift 来移除广告牌."),
	SIGN_REMOVED("&a广告牌已移除."),
	ADDED_SIGN("&a这个广告牌已成功创建! 创建者: &7{2} &a, &b{0} 金币 &a价格租用 &b{1} 天&a.",
			"0: 价格  1: 租用时间(天)  2: 创建者名字  3: 创建者 uuid"),
	ALREADY_BILLBOARD_SIGN("&7这个牌子已经是广告牌了."),
	NO_TARGETED_SIGN("&7你必须要用准星指向一个牌子."),
	ONLY_AS_PLAYER("控制台无法使用该命令."),
	INFO_HEADER("&b广告牌 &7- &f详细信息"),
	INFO_CREATOR("&7|- &f创建者: &e{0}",
			"0: 创建者名字  1: 创建者 uuid"),
	INFO_OWNER("&7|- &f租用者: &e{0}",
			"0: 租用者名字  1: 租用者 uuid"),
	INFO_PRICE("&7|- &f价格: &e{0} 金币",
			"0: 价格"),
	INFO_DURATION("&7|- &f租用时间: &e{0} 天",
			"0: 租用时间(天)"),
	INFO_RENT_SINCE("&7|- &f从 &e{0} &f起开始租用",
			"0: 租用开始日期"),
	INFO_RENT_UNTIL("&7|- &f在 &e{0} &f租用到期",
			"0: 租用结束日期"),
	INFO_TIME_LEFT("&7|- &f剩余时间: &e{0}",
			"0: 租用剩余时间"),
	CLICK_TO_RENT("&b&lBillboards &7>> &f再次点击牌子, 从 &e{2} &f租用这个广告牌. 租用价格为 &e{0}&f, 租期为 &e{1} &f天.",
			"0: 价格  1: 租用时间(天)  2: 租用者名字  3: 租用者 uuid"),
	YOU_HAVE_RENT_A_SIGN("&b&lBillboards &7>> &a你成功从 &7{2} 租用了这块广告牌! 租期为 &b{1} 天&a. \n" +
			"&e要编辑广告牌, 请 &fShift+右键 &e点击你的牌子.",
			"0: 价格  1: 租用时间(天)  2: 创建者名字  3: 创建者 uuid  4: 租用者名字  5: 租用者 uuid"),
	TRANSACTION_FAILURE("&c出现错误: &6{0}",
			"0: 错误信息"),
	NO_LONGER_AVAILABLE("&b&lBillboards &7>> &c这个广告牌已经被别人租用了!"),
	INVALID_BILLBOARD("&b&lBillboards &7>> &c该广告牌无效."),
	NOT_ENOUGH_MONEY("&c&b&lBillboards &7>> &c你没有足够的金钱! \n" +
			"&c你需要 &e{0} 金币&c来租用广告牌, 但你只有 &e{1} 金币&c!",
			"0: 价格  1: 玩家金钱数量"),
	MAX_RENT_LIMIT_REACHED("&b&lBillboards &7>> &c你已经不能再拥有更多的广告牌了 &7(限制: &6{0}&7)&c!",
			"0: 限制租赁次数"),
	CANT_RENT_OWN_SIGN("&b&lBillboards &7>> &c你不能租用自己创建的牌子."),
	NO_PERMISSION("&b&lBillboards &7>> &c你没有执行该操作的权限."),
	PLAYER_NOT_FOUND("&b&lBillboards &7>> &c无法找到玩家 &6{0}",
			"0: 玩家名字"),
	SIGN_LINE_1("&b广告牌",
			"0: 价格  1: 租用时间(天)  2: 创建者  3: 创建者 uuid"),
	SIGN_LINE_2("&f(右键租用!)",
			"0: 价格  1: 租用时间(天)  2: 创建者  3: 创建者 uuid"),
	SIGN_LINE_3("&0{0} 金币",
			"0: 价格  1: 租用时间(天)  2: 创建者  3: 创建者 uuid"),
	SIGN_LINE_4("&0租用 {1} 天",
			"0: 价格  1: 租用时间(天)  2: 创建者  3: 创建者 uuid"),
	DATE_FORMAT("yyyy/MM/dd HH:mm:ss",
			"日期格式"),
	TIME_REMAINING_FORMAT("%d 天 %d 时 %d 分",
			"剩余时间格式"),
	INVALID_NUMBER("&c无效的数字: &6{0}",
			"0: 无效的参数"),
	RENT_SIGN_LINE_1("&a租用者",
			"0: 价格  1: 租用时间(天)  2: 创建者  3: 创建者 uuid  4: 租用者  5: 租用者 uuid"),
	RENT_SIGN_LINE_2("&f{4}",
			"0: 价格  1: 租用时间(天)  2: 创建者  3: 创建者 uuid  4: 租用者  5: 租用者 uuid"),
	RENT_SIGN_LINE_3("&cShift+右键",
			"0: 价格  1: 租用时间(天)  2: 创建者  3: 创建者 uuid  4: 租用者  5: 租用者 uuid"),
	RENT_SIGN_LINE_4("&c编辑广告牌",
			"0: 价格  1: 租用时间(天)  2: 创建者  3: 创建者 uuid  4: 租用者  5: 租用者 uuid"),
	RELOADED("&b&lBillboards &7>> &a配置文件和语言已重载."),
	PROMPT_START("&b&lBillboards &7>> &e请在聊天栏发送领地名. 发送 &f#cancel &e代表取消该操作."),
	PROMPT_FAILED("&b&lBillboards &7>> &e你输入的领地名无效，请重新发送.",
			"0: 玩家名  1: 玩家输入的参数"),
	PROMPT_SUCCESS("&b&lBillboards &7>> &a你已成功设置广告牌点击操作.",
			"0: 玩家名  1: 玩家输入的参数"),
	PROMPT_CANCELLED("&b&lBillboards &7>> &f参数补全已取消.",
			"0: 玩家名"),

	;
	public final String defaultText;
	@Nullable
	public final String note;
	Message(String defaultText) {
		this(defaultText, null);
	}
    Message(String defaultText, @Nullable String note) {
        this.defaultText = defaultText;
        this.note = note;
    }

    public String get(String... args) {
		String message = Messages.getMessage(this);
		for (int i = 0; i < args.length; i++) {
			String param = args[i];
			message = message.replace("{" + i + "}", param);
		}
		return message;
	}
}
