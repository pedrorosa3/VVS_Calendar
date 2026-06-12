package com.example.meetings.e2e;

import com.ninja_squad.dbsetup.DbSetup;
import com.ninja_squad.dbsetup.destination.DataSourceDestination;
import com.ninja_squad.dbsetup.operation.Operation;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.security.crypto.password.PasswordEncoder;

import javax.sql.DataSource;

import static com.ninja_squad.dbsetup.Operations.*;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class CalendarE2ETest {

    @LocalServerPort
    private int port;

    @Autowired
    private DataSource dataSource;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private WebDriver driver;

    @BeforeEach
    void setUp() {
        // 1. Preparar a Base de Dados (Testing Database) com o DbSetup
        String hashedPass = passwordEncoder.encode("senha123");
        Operation operation = sequenceOf(
                deleteAllFrom("meeting_participants", "meetings", "users"),
                insertInto("users")
                        .columns("id", "username", "email", "password_hash", "ical_token")
                        .values(1L, "pedro_e2e", "pedro@fc.ul.pt", hashedPass, "token-e2e")
                        .build()
        );
        new DbSetup(new DataSourceDestination(dataSource), operation).launch();

        // 2. Configurar o Selenium WebDriver em modo invisível (Headless)
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless=new"); // Corre sem abrir a janela visual (ideal para o CI/GitHub Actions)
        options.addArguments("--remote-allow-origins=*");
        driver = new ChromeDriver(options);
    }

    @AfterEach
    void tearDown() {
        if (driver != null) {
            driver.quit(); // Fechar o browser após o teste
        }
    }

    @Test
    void userCanLoginAndProposeMeetingSuccessfully() throws InterruptedException {
        String baseUrl = "http://localhost:" + port;

        // 1. Navegar para a página de Login
        driver.get(baseUrl + "/login");
        
        // 2. Fazer Login
        driver.findElement(By.name("username")).sendKeys("pedro_e2e");
        driver.findElement(By.name("password")).sendKeys("senha123");
        driver.findElement(By.cssSelector("button[type='submit']")).click();


        Thread.sleep(1000);

        // 3. Validar se entrou no Calendário
        assertEquals(baseUrl + "/calendar", driver.getCurrentUrl(), "Deve ser redirecionado para o calendário");
        
        // 4. Navegar para criar nova reunião
        driver.get(baseUrl + "/meetings/new");

        // 5. Preencher o formulário de Proposta de Reunião
        String meetingTitle = "Defesa Final de VVS";
        driver.findElement(By.name("title")).sendKeys(meetingTitle);
        driver.findElement(By.name("description")).sendKeys("Apresentação do Projeto 2");
        
        WebElement startInput = driver.findElement(By.name("start"));
        WebElement endInput = driver.findElement(By.name("end"));

        org.openqa.selenium.JavascriptExecutor js = (org.openqa.selenium.JavascriptExecutor) driver;
        js.executeScript("arguments[0].value = '2026-06-15T10:00';", startInput);
        js.executeScript("arguments[0].value = '2026-06-15T11:30';", endInput);

        // Submeter o formulário
        driver.findElement(By.name("title")).submit();

        Thread.sleep(1000);

        // 6. Validar o resultado
        assertEquals(baseUrl + "/calendar", driver.getCurrentUrl(), "Deve voltar ao calendário após propor");
        
        // Procurar o título da reunião no HTML da página do calendário
        String pageSource = driver.getPageSource();
        assertTrue(pageSource.contains(meetingTitle), "O título da nova reunião deve aparecer na página");
    }
}