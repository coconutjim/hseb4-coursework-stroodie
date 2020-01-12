package controllers;

import controllers.mappers.util.MessageFixedPoint;
import org.apache.commons.validator.routines.EmailValidator;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Validation Utils
 */
public class Validation {

    // Constants
    private final static Pattern NAME_PATTERN = Pattern.compile("^[\\p{L} .'-]{1,20}$");
    private final static Pattern PASSWORD_PATTERN = Pattern.compile("^[A-Za-z0-9]{6,20}$");

    // User
    public final static int PASSWORD_MIN_LENGTH = 6;
    public final static int PASSWORD_MAX_LENGTH = 20;
    public final static int FIRSTNAME_MIN_LENGTH = 1;
    public final static int FIRSTNAME_MAX_LENGTH = 20;
    public final static int LASTNAME_MIN_LENGTH = 1;
    public final static int LASTNAME_MAX_LENGTH = 20;

    // Seminar
    public final static int SEMINAR_NAME_MIN_LENGTH = 2;
    public final static int SEMINAR_NAME_MAX_LENGTH = 50;
    public final static int SEMINAR_DESCR_MIN_LENGTH = 2;
    public final static int SEMINAR_DESCR_MAX_LENGTH = 250;
    public final static int TYPE_NAME_MIN_LENGTH = 2;
    public final static int TYPE_NAME_MAX_LENGTH = 20;

    // Discussion
    public final static int DISCUSSION_NAME_MIN_LENGTH = 2;
    public final static int DISCUSSION_NAME_MAX_LENGTH = 50;
    public final static int DISCUSSION_DESCR_MIN_LENGTH = 2;
    public final static int DISCUSSION_DESCR_MAX_LENGTH = 250;

    // Message
    public final static int MESSAGE_CONTENT_MIN_LENGTH = 1;
    public final static int MESSAGE_CONTENT_MAX_LENGTH = 20000;
    public final static int MESSAGE_TEXT_MIN_LENGTH = 1;
    public final static int MESSAGE_TEXT_MAX_LENGTH = 10000;

    // Union
    public final static int UNION_NAME_MIN_LENGTH = 2;
    public final static int UNION_NAME_MAX_LENGTH = 10;

    // Thesaurus
    public final static int THESAURUS_NAME_MIN_LENGTH = 2;
    public final static int THESAURUS_NAME_MAX_LENGTH = 20;
    public final static int THESAURUS_CONTENT_MIN_LENGTH = 2;
    public final static int THESAURUS_CONTENT_MAX_LENGTH = 6000;
    public final static int THESAURUS_TEXT_MIN_LENGTH = 2;
    public final static int THESAURUS_TEXT_MAX_LENGTH = 3000;

    // Error messages

    public final static String EMAIL_ERROR_MESSAGE = "Введенный email некорректен!";
    public final static String EMAIL_UN_ERROR_MESSAGE = "Такой email уже зарегистрирован в системе!";
    public final static String PASSWORD_ERROR_MESSAGE = "Пароль должен иметь длину от " +
            PASSWORD_MIN_LENGTH + " до " + PASSWORD_MAX_LENGTH + " символов и состоять из латинских букв и цифр!";
    public final static String PASSWORD_DIF_ERROR_MESSAGE = "Введенные пароли не совпадают!";
    public final static String PASSWORD_CURR_ERROR_MESSAGE = "Текущий пароль введен неверно!";
    public final static String PASSWORD_MATCH_ERROR_MESSAGE = "Введеный пароль совпадает со старым!";
    public final static String FIRSTNAME_ERROR_MESSAGE = "Имя должно иметь длину от " +
            FIRSTNAME_MIN_LENGTH + " до " + FIRSTNAME_MAX_LENGTH + " символов и состоять из " +
            "русских/латинских букв и знаков \".\",\"'\",\"-\"!";
    public final static String LASTNAME_ERROR_MESSAGE = "Фамилия должна иметь длину от " +
            LASTNAME_MIN_LENGTH + " до " + LASTNAME_MAX_LENGTH + " символов и состоять из " +
            "русских/латинских букв и знаков \".\",\"'\",\"-\"!";
    public final static String SEMINAR_NAME_ERROR_MESSAGE = "Имя семинара должно иметь длину от " +
            SEMINAR_NAME_MIN_LENGTH + " до " + SEMINAR_NAME_MAX_LENGTH + " символов!";
    public final static String SEMINAR_DESCR_ERROR_MESSAGE = "Описание семинара должно иметь длину от " +
            SEMINAR_DESCR_MIN_LENGTH + " до " + SEMINAR_DESCR_MAX_LENGTH + " символов!";
    public final static String TYPE_NAME_ERROR_MESSAGE = "Имя типа должно иметь длину от " +
            TYPE_NAME_MIN_LENGTH + " до " + TYPE_NAME_MAX_LENGTH + " символов!";
    public final static String SEMINAR_MANY_MASTERS_ERROR_MESSAGE = "У семинара может быть только 1 руководитель!";
    public final static String SEMINAR_NO_MASTER_ERROR_MESSAGE = "У семинара должен быть руководитель!";
    public final static String TYPE_NAME_DIF_ERROR_MESSAGE = "Имена типов должны различаться!";
    public final static String SEMINAR_NO_ONTOLOGY_ERROR_MESSAGE = "В семинаре без онтологии не может быть дискуссий!";
    public final static String DISCUSSION_NAME_ERROR_MESSAGE = "Имя дискуссии должно иметь длину от " +
            DISCUSSION_NAME_MIN_LENGTH + " до " + DISCUSSION_NAME_MAX_LENGTH + " символов!";
    public final static String DISCUSSION_DESCR_ERROR_MESSAGE = "Описание дискуссии должно иметь длину от " +
            DISCUSSION_DESCR_MIN_LENGTH + " до " + DISCUSSION_DESCR_MAX_LENGTH + " символов!";
    public final static String DISCUSSION_NO_MASTER_ERROR_MESSAGE = "У дискуссии должен быть руководитель!";
    public final static String MESSAGE_TEXT_ERROR_MESSAGE = "Сообщение должно иметь длину от " +
            MESSAGE_TEXT_MIN_LENGTH + " до " + MESSAGE_TEXT_MAX_LENGTH + " символов!";
    public final static String MESSAGE_FORMAT_ERROR_MESSAGE = "Неверный формат сообщения!";
    public final static String UNION_NAME_ERROR_MESSAGE = "Имя объединения должно иметь длину от " +
            UNION_NAME_MIN_LENGTH + " до " + UNION_NAME_MAX_LENGTH + " символов!";
    public final static String GLOBAL_UNION_ERROR_MESSAGE = "Невозможно свернуть дискуссию в одно объединение!";

    public final static String THESAURUS_NAME_ERROR_MESSAGE = "Термин должен иметь длину от " +
            THESAURUS_NAME_MIN_LENGTH + " до " + THESAURUS_NAME_MAX_LENGTH + " символов!";
    public final static String THESAURUS_TEXT_ERROR_MESSAGE = "Значение термина должно иметь длину от " +
            THESAURUS_TEXT_MIN_LENGTH + " до " + THESAURUS_TEXT_MAX_LENGTH + " символов!";
    public final static String THESAURUS_FORMAT_ERROR_MESSAGE = "Неверный формат определения!";


    // Validation methods

    public static boolean isPasswordValid(String password) {
        return password != null && PASSWORD_PATTERN.matcher(password).matches();
    }

    public static boolean isEmailValid(String email) {
        //TODO: consider allowing local
        return email != null && ! email.isEmpty() && EmailValidator.getInstance(true).isValid(email);
    }

    public static boolean isFirstNameValid(String name) {
        return name != null && NAME_PATTERN.matcher(name).matches();
    }

    public static boolean isLastNameValid(String name) {
        return name != null && NAME_PATTERN.matcher(name).matches();
    }

    public static boolean isModeValid(String mode) {
        return mode != null && (mode.equals("create") || mode.equals("update") || mode.equals("delete"));
    }

    public static boolean isSeminarNameValid(String name) {
        return name != null && name.length() >= SEMINAR_NAME_MIN_LENGTH && name.length() <= SEMINAR_NAME_MAX_LENGTH;
    }

    public static boolean isSeminarDescriptionValid(String description) {
        return description == null || description.isEmpty() ||
                (description.length() >= SEMINAR_DESCR_MIN_LENGTH && description.length() <= SEMINAR_DESCR_MAX_LENGTH);
    }

    public static boolean isTypeNameValid(String name) {
        return name != null && name.length() >= TYPE_NAME_MIN_LENGTH && name.length() <= TYPE_NAME_MAX_LENGTH;
    }

    public static boolean isTypeIconNameValid(String iconName) {
        return iconName != null && ! iconName.isEmpty();
    }

    public static boolean isTypeIconValueValid(String iconValue) {
        return iconValue != null&& ! iconValue.isEmpty();
    }

    public static boolean isDiscussionNameValid(String name) {
        return name != null && name.length() >= DISCUSSION_NAME_MIN_LENGTH && name.length() <= DISCUSSION_NAME_MAX_LENGTH;
    }

    public static boolean isDiscussionDescriptionValid(String description) {
        return description == null || description.isEmpty() ||
                (description.length() >= DISCUSSION_DESCR_MIN_LENGTH && description.length() <= DISCUSSION_DESCR_MAX_LENGTH);
    }

    public static boolean isMessageContentValid(String content) {
        return content != null && content.length() >= MESSAGE_CONTENT_MIN_LENGTH && content.length() <= MESSAGE_CONTENT_MAX_LENGTH;
    }

    public static boolean isMessageTextValid(String text) {
        return text != null && text.length() >= MESSAGE_TEXT_MIN_LENGTH && text.length() <= MESSAGE_TEXT_MAX_LENGTH;
    }

    public static boolean isSearchTypeValid(String type) {
        return type != null && (type.equals("type") || type.equals("user") || type.equals("text") || type.equals("date"));
    }

    public static DateTime validateAndParseSearchDate(String date) {
        if (date == null || date.isEmpty()) {
            return null;
        }
        DateTime result;
        try {
            date += " 00:00:00";
            DateTimeFormatter formatter = DateTimeFormat.forPattern("dd.MM.yyyy HH:mm:ss");
            result = formatter.parseDateTime(date);
            return result;
        }
        catch (Exception e) {
            return null;
        }
    }

    public static boolean isSearchTextValid(String text) {
        return text != null && ! text.isEmpty();
    }

    public static boolean isValidGraphPositions(Map<Integer, MessageFixedPoint> positions) {
        return positions != null && ! positions.isEmpty();
    }

    public static boolean isUnionNameValid(String name) {
        return name != null && name.length() >= UNION_NAME_MIN_LENGTH && name.length() <= UNION_NAME_MAX_LENGTH;
    }

    public static boolean isUnionContentValid(List<Integer> mesIds, List<Integer> unionIds) {
        int count = 0;
        if (mesIds != null) {
            count += mesIds.size();
        }
        if (unionIds != null) {
            count += unionIds.size();
        }
        return count > 1;
    }

    public static boolean isThesaurusNameValid(String name) {
        return name != null && name.length() >= THESAURUS_NAME_MIN_LENGTH && name.length() <= THESAURUS_NAME_MAX_LENGTH;
    }

    public static boolean isThesaurusContentValid(String content) {
        return content != null && content.length() >= THESAURUS_CONTENT_MIN_LENGTH &&
                content.length() <= THESAURUS_CONTENT_MAX_LENGTH;
    }

    public static boolean isThesaurusTextValid(String text) {
        return text != null && text.length() >= THESAURUS_TEXT_MIN_LENGTH &&
                text.length() <= THESAURUS_TEXT_MAX_LENGTH;
    }
}
