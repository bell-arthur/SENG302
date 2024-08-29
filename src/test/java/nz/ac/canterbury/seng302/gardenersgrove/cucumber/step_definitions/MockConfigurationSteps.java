package nz.ac.canterbury.seng302.gardenersgrove.cucumber.step_definitions;


import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import nz.ac.canterbury.seng302.gardenersgrove.entity.Gardener;
import nz.ac.canterbury.seng302.gardenersgrove.entity.IdentifiedPlant;
import nz.ac.canterbury.seng302.gardenersgrove.service.EmailUserService;
import nz.ac.canterbury.seng302.gardenersgrove.service.GardenerFormService;
import nz.ac.canterbury.seng302.gardenersgrove.service.PlantIdentificationService;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
public class MockConfigurationSteps {
    @Autowired
    private EmailUserService emailUserService;
    @Autowired
    private PlantIdentificationService plantIdentificationService;
    @Autowired
    private GardenerFormService gardenerFormService;


    @Then("send an email.")
    public void send_an_email() {
        Mockito.verify(emailUserService, Mockito.times(1)).sendEmail(anyString(), anyString(), anyString());
        Mockito.doNothing().when(emailUserService).sendEmail(anyString(),anyString(),anyString());
    }

    @Then("I receive an email confirming my account has been blocked for one week")
    public void send_ban_email() {
        Mockito.verify(emailUserService, Mockito.times(1)).sendEmail(anyString(), anyString(), anyString());
        Mockito.doNothing().when(emailUserService).sendEmail(anyString(),anyString(),anyString());
    }

    @When("the app identifies the plant image")
    public void the_app_identifies_the_plant_image() throws IOException {
        Optional<Gardener> gardenerOptional = gardenerFormService.findByEmail("a@gmail.com");
        Gardener gardener = gardenerOptional.get();
        IdentifiedPlant testIdentifiedPlant = new IdentifiedPlant(
                "Helianthus annuus",
                0.88,
                List.of("Sunflower", "Rose"),
                "5414641",
                "https://example.com/sunflower.jpg",
                "https://example.com/sunflower.jpg"
                ,gardener
        );
        when(plantIdentificationService.identifyPlant(
                Mockito.any(MultipartFile.class),
                Mockito.any(Gardener.class)))
                .thenReturn(testIdentifiedPlant);
    }

}
