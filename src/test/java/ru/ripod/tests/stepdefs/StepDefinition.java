package ru.ripod.tests.stepdefs;

import cucumber.api.java.After;
import cucumber.api.java.AfterStep;
import cucumber.api.java.Before;
import cucumber.api.java.ru.И;
import cucumber.api.java.ru.Когда;
import cucumber.api.java.ru.Тогда;
import io.qameta.allure.Step;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.ripod.tests.pageobjects.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Properties;


public class StepDefinition {
    //thread locals for variables used throughout whole test

    private static class TestDataManager {
        private static ThreadLocal<HashMap<String, String>> storage = new ThreadLocal<>();

        public static void init() {
            storage.set(new HashMap<>());
        }

        public static String getValue(String key) {

            String value = storage.get().get(key);
            if (value != null) {
                logger.debug("Получение данных по ключу {}. Получено значение {}", key, value);
                return value;
            } else {
                logger.warn("Получение данных по ключу {}. Значение отсутствует или пустое", key);
                return value;
            }
        }

        public static void setValue(String key, String value) {
            String prevValue = storage.get().put(key, value);
            if (prevValue == null) {
                logger.debug("Добавлены данные. Ключ: {}, значение: {}", key, value);
            } else {
                logger.info("Заменены данные. Ключ: {}, прошлое значение: {}, новое значение: {}", key, prevValue, value);
            }

        }
    }


    private static Logger logger = LogManager.getRootLogger();
    //page object declaration
    private BasicPage basicPage;
    private SearchPage searchPage;
    private AuthorizationPage authorizationPage;
    private MailPage mailPage;
    private DraftPage draftPage;
    private SentPage sentPage;

    public static void setUsedBrowser(String browserName) {
        TestDataManager.init();
        TestDataManager.setValue("usedBrowser", browserName);
    }

    public static String getUsedBrowser() {
        return TestDataManager.getValue("usedBrowser");
    }

    /**
     * Инициализация браузера и страничных объектов
     */
    @Step("Инициализация браузера и страничных объектов")
    @Before
    public void browserInit() {
        String browserString = getUsedBrowser();
        basicPage = new BasicPage(browserString);
        searchPage = new SearchPage(browserString);
        authorizationPage = new AuthorizationPage(browserString);
        mailPage = new MailPage(browserString);
        draftPage = new DraftPage(browserString);
        sentPage = new SentPage(browserString);
    }

    @Step("Скриншот после шага")
    @AfterStep
    public void attachScreenshotAsStep() {
        basicPage.takeScreenshot();
    }

    @Step(value = "Открытие страницы {0}")
    @Когда("открываем страницу {string} в браузере")
    public void openPageInBrowser(String url) {
        basicPage.openPage(url);
        logger.info("Открытие страницы {} в браузере {}", url, getUsedBrowser());
    }

    @Step("Нажатие кнопки {0} на главной странице")
    @И("нажимаем кнопку {string} на главной странице")
    public void pressMailButtonSearchPage(String buttonName) {
        searchPage.clickHeaderButton(buttonName);
        logger.info("Нажатие кнопки {} на главной странице", buttonName);
    }

    @Step("Нажатие кнопки \"Войти\"")
    @И("нажимаем кнопку \"Войти\"")
    public void clickSignInButton() {
        searchPage.clickSignInButton();
        logger.info("Нажатие кнопки \"Войти\" на стартовой странице почты");
    }

    @Step("Ввод логина из файла \"{0}\"")
    @И("вводим логин из файла {string}")
    public void inputLoginFromFile(String fileName) {
        Properties credProperties = new Properties();
        try {
            InputStream credInputStream = new FileInputStream(fileName);
            credProperties.load(credInputStream);
        } catch (IOException e) {
            logger.warn("Problem reading properties file");
        }
        TestDataManager.setValue("login", credProperties.getProperty(getUsedBrowser() + "login"));
        authorizationPage.switchToNextTab();
        authorizationPage.inputLogin(TestDataManager.getValue("login"));
        logger.info("Ввод логина");
    }

    @Step("Нажатие кнопки \"{0}\" на странице авторизации")
    @И("нажимаем кнопку {string} на странице авторизации")
    public void pressAuthPageButton(String buttonName) {
        authorizationPage.pressButton(buttonName);
        logger.info("Нажатие кнопки {} на странице авторизации", buttonName);
    }

    @Step("Ввод пароля из файла \"{0}\"")
    @И("вводим пароль из файла {string}")
    public void inputPasswordFromFile(String fileName) {
        Properties credProperties = new Properties();
        try {
            InputStream credInputStream = new FileInputStream(fileName);
            credProperties.load(credInputStream);
        } catch (IOException e) {
            logger.warn("Problem reading properties file");
        }
        String password = credProperties.getProperty(getUsedBrowser() + "password");
        authorizationPage.inputPassword(password);
        logger.info("Ввод пароля");
    }

    @Step("Проверка открытия страницы почты")
    @Тогда("открывается главная страница почты")
    public void checkMailPage() {
        mailPage.checkPageOpened();
        logger.info("Проверка открытия страницы почты");
    }

    @Step("Нажатие кнопки создания письма")
    @Когда("нажимаем кнопку создания письма")
    public void pressButtonCreateLetter() {
        mailPage.pressCreateLetter();
        logger.info("Нажатие кнопки создания письма");
    }

    @Step("Ввод получателя из файла {0}")
    @И("указываем получателя из файла {string}")
    public void inputReceiverEmailFromFile(String fileName) {
        Properties credProperties = new Properties();
        try {
            InputStream credInputStream = new FileInputStream(fileName);
            credProperties.load(credInputStream);
        } catch (IOException e) {
            logger.warn("Problem reading properties file");
        }
        String email = credProperties.getProperty(getUsedBrowser() + "receiver");
        mailPage.inputReceiverEmail(email);
        logger.info("Ввод получателя");
    }

    @Step("Ввод темы письма")
    @И("указываем тему с указанием даты и времени")
    public void inputThemeWithCurrentTime() {
        Date mailDateRaw = new Date();
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.UK);
        TestDataManager.setValue("mailDate", dateFormat.format(mailDateRaw));
        TestDataManager.setValue("mailTheme", String.format("%s mail %s", getUsedBrowser(), TestDataManager.getValue("mailDate")));
        mailPage.inputMailTheme(TestDataManager.getValue("mailTheme"));
        logger.info("Ввод темы нового письма");
    }

    @Step("Ввод текста письма")
    @И("вводим текст письма")
    public void inputMailBody() {
        String bodyBase = "Это письмо написано в браузере %s. Дата и время: %s";
        TestDataManager.setValue("mailBody", String.format(bodyBase, getUsedBrowser(), TestDataManager.getValue("mailDate")));
        mailPage.inputMailBody(TestDataManager.getValue("mailBody"));
        logger.info("Ввод текста нового письма");
    }

    @Step("Закрытие окна создания письма")
    @И("закрываем окно создания письма")
    public void closeNewLetterWindow() {
        mailPage.closeWhenDraftSaved();
        logger.info("Закрытие окна создания письма после сохранения черновика");
    }

    @Step("Открытие страницы \"{0}\" в почте")
    @И("открываем страницу {string} в почте")
    public void openPagePartInMail(String partName) {
        logger.info("Открытие раздела {} в почте", partName);
        mailPage.openMailsPagePart(partName);
    }

    @Step("Проверка наличия созданного черновика")
    @Тогда("в списке писем содержится созданный нами черновик")
    public void checkCreatedDraftVisible() {
        logger.info("Проверка наличия созданного ранее черновика");
        draftPage.checkCreatedDraftVisible(TestDataManager.getValue("mailTheme"));
    }

    @Step("Выбор созданного черновика")
    @Когда("нажимаем на созданный черновик")
    public void openCreatedDraft() {
        logger.info("Выбор созданного ранее черновика");
        draftPage.openCreatedDraft(TestDataManager.getValue("mailTheme"));
    }

    @Step("Проверка открытия окна создания письма")
    @Тогда("открывается окно создания или редактирования письма")
    public void checkLetterCreateEditIsOpened() {
        logger.info("Проверка открытия окна создания письма");
        mailPage.checkLetterCreateEditIsOpened();
    }

    @Step("Проверка адреса получателя из файла \"{0}\"")
    @И("в поле получателя указан адрес из файла {string}")
    public void checkReceiverEmailFromFile(String fileName) {
        logger.info("Проверка адреса получателя");
        Properties credProperties = new Properties();
        try {
            InputStream credInputStream = new FileInputStream(fileName);
            credProperties.load(credInputStream);
        } catch (IOException e) {
            logger.warn("Problem reading properties file");
        }
        String email = credProperties.getProperty(getUsedBrowser() + "receiver");
        mailPage.checkEmailValue(email);
    }

    @Step("Проверка темы письма созданного черновика")
    @И("тема письма соответствует теме созданного черновика")
    public void checkMailTheme() {
        logger.info("Проверка темы письма");
        mailPage.checkThemeValue(TestDataManager.getValue("mailTheme"));
    }

    @Step("Проверка текста письма созданного черновика")
    @И("текст письма соответствует тексту созданного письма")
    public void checkMailText() {
        logger.info("Проверка текста письма");
        mailPage.checkBodyValue(TestDataManager.getValue("mailBody"));
    }

    @Step("Отправка созданного черновика")
    @Когда("нажимаем кнопку \"Отправить\" в окне нового письма")
    public void pressSendInMailWindow() {
        logger.info("Отправка созданного черновика");
        mailPage.pressSendButton();
    }

    @Step("Проверка отсутствия отправленного письма в списке черновиков")
    @Тогда("в списке черновиков не содержится созданный нами черновик")
    public void checkDraftNotShown() {
        logger.info("Проверка отсутствия отправленного письма в списке черновиков");
        draftPage.checkCreatedDraftNotVisible(TestDataManager.getValue("mailTheme"));
    }

    @Step("Проверка наличия отправленного письма в списке отправленных писем")
    @Тогда("в списке писем содержится отправленное нами письмо")
    public void checkSentLetterIsShown() {
        logger.info("Проверка наличия отправленного письма в списке отправленных писем");
        sentPage.checkSentMailIsVisible(TestDataManager.getValue("mailTheme"));
    }

    @Step("Выход из аккаунта")
    @Когда("нажимаем на кнопку аккаунта и нажимаем \"Выйти\"")
    public void pressSignOutButton() {
        logger.info("Выход из аккаунта");
        mailPage.pressSignOutButton();
    }

    @Step("Проверка выхода из аккаунта")
    @Тогда("выходим из аккаунта")
    public void checkSignedOut() {
        logger.info("Проверка выхода из аккаунта");
        authorizationPage.checkSignedOut(TestDataManager.getValue("login"));
    }

    @Step("Закрытие браузера")
    @After
    public void closeBrowser() {
        basicPage.closeBrowser();
    }

}
