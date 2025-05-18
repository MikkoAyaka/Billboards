package de.blablubbabc.billboards.message;

import org.jetbrains.annotations.Nullable;

public enum Message {

	UNKNOWN_NAME("unknown",
			"Used to represent an unknown player name"),
	UNKNOWN_UUID("-",
			"Used to represent an unknown uuid"),
	SERVER_OWNER_NAME("SERVER",
			"Used to represent billboards created or owned by the server"),
	YOU_HAVE_TO_SNEAK("&7You have to sneak to remove this."),
	SIGN_REMOVED("&aBillboard sign was removed."),
	ADDED_SIGN("&aThis sign can now be rented from &7{2} &afor &b{0} $ &afor &b{1} days&a.",
			"0: price  1: duration  2: creator name  3: creator uuid"),
	ALREADY_BILLBOARD_SIGN("&7This sign is already a billboard sign."),
	NO_TARGETED_SIGN("&7You have to target a sign."),
	ONLY_AS_PLAYER("This only works as player."),
	INFO_HEADER("&3Billboard - Information"),
	INFO_CREATOR("&5Creator: &2{0}",
			"0: creator name  1: creator uuid"),
	INFO_OWNER("&5Owner: &2{0}",
			"0: owner name  1: owner uuid"),
	INFO_PRICE("&5Price: &2{0} $",
			"0: price"),
	INFO_DURATION("&5Duration: &2{0} days",
			"0: duration"),
	INFO_RENT_SINCE("&5Rented since: &2{0}",
			"0: since date"),
	INFO_RENT_UNTIL("&5Rented until: &2{0}",
			"0: until date"),
	INFO_TIME_LEFT("&5Time remaining: &2{0}",
			"0: time left"),
	CLICK_TO_RENT("&6Click the sign again, to rent it from &7{2} &6for &b{0} $ &6for &b{1} days&6.",
			"0: price  1: duration  2: creator name  3: creator uuid"),
	YOU_HAVE_RENT_A_SIGN("&aYou have rented this sign now from &7{2} &afor &b{1} days&a. \n" +
			"&bTo edit it: &aSneak and right-click it.",
			"0: price  1: duration  2: creator name  3: creator uuid  4: owner name  5: owner uuid"),
	TRANSACTION_FAILURE("&cSomething went wrong: &6{0}",
			"0: errorMessage"),
	NO_LONGER_AVAILABLE("&cThis sign is no longer available!"),
	NOT_ENOUGH_MONEY("&cYou have not enough money! \n" +
			"You need &6{0} $&c, but you only have &6{1} $&c!",
			"0: price  1: balance"),
	MAX_RENT_LIMIT_REACHED("&cYou already own too many billboard signs &7(limit: &6{0}&7)&c!",
			"0: limit"),
	CANT_RENT_OWN_SIGN("&cYou can't rent your own sign."),
	NO_PERMISSION("&cYou have no permission for that."),
	PLAYER_NOT_FOUND("&cCouldn't find player &6{0}",
			"0: player name"),
	SIGN_LINE_1("&bRENT ME",
			"0: price  1: duration  2: creator name  3: creator uuid"),
	SIGN_LINE_2("&f(right-click!)",
			"0: price  1: duration  2: creator name  3: creator uuid"),
	SIGN_LINE_3("&8{0} $",
			"0: price  1: duration  2: creator name  3: creator uuid"),
	SIGN_LINE_4("&8{1} days",
			"0: price  1: duration  2: creator name  3: creator uuid"),
	DATE_FORMAT("dd/MM/yyyy HH:mm:ss",
			"Only change this if you know what you are doing.."),
	TIME_REMAINING_FORMAT("%d days %d h %d min",
			"Only change this if you know what you are doing.."),
	INVALID_NUMBER("&cInvalid number: &6{0}",
			"0: the invalid argument"),
	RENT_SIGN_LINE_1("&aRent by",
			"0: price  1: duration  2: creator name  3: creator uuid  4: owner name  5: owner uuid"),
	RENT_SIGN_LINE_2("&f{4}",
			"0: price  1: duration  2: creator name  3: creator uuid  4: owner name  5: owner uuid"),
	RENT_SIGN_LINE_3("&cSneak & right-",
			"0: price  1: duration  2: creator name  3: creator uuid  4: owner name  5: owner uuid"),
	RENT_SIGN_LINE_4("&cclick to edit",
			"0: price  1: duration  2: creator name  3: creator uuid  4: owner name  5: owner uuid"),
	RELOADED("&aAll configurations and messages has been reloaded."),
	PROMPT_START("&7[&bBillboards&7] &ePlease send your residence name via game chat. Send &f#cancel &emeans cancel the process."),
	PROMPT_FAILED("&7[&bBillboards&7] &eThe name you input is invalid. Please send again.",
			"0: player name  1: what player input"),
	PROMPT_SUCCESS("&7[&bBillboards&7] &aYou have been set the click action of your Billboard",
			"0: player name  1: what player input"),
	PROMPT_CANCELLED("&7[&bBillboards&7] &fPrompt cancelled",
			"0: player name"),

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
