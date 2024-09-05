package nz.ac.canterbury.seng302.gardenersgrove.controller;

import nz.ac.canterbury.seng302.gardenersgrove.entity.Garden;
import nz.ac.canterbury.seng302.gardenersgrove.entity.Gardener;
import nz.ac.canterbury.seng302.gardenersgrove.entity.Plant;
import nz.ac.canterbury.seng302.gardenersgrove.entity.WikiPlant;
import nz.ac.canterbury.seng302.gardenersgrove.service.*;
import nz.ac.canterbury.seng302.gardenersgrove.util.ValidityChecker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;


import java.io.IOException;
import java.math.BigDecimal;
import java.net.URISyntaxException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

/**
 * The controller for the plant wiki page. It handles all requests for going to the page and searching for plant information.
 */
@Controller
public class PlantWikiController {

    Logger logger = LoggerFactory.getLogger(PlantWikiController.class);

    private final PlantWikiService plantWikiService;

    private final PlantService plantService;

    private final GardenerFormService gardenerFormService;

    private final GardenService gardenService;

    private final ImageService imageService;



    private Gardener gardener;




    @Autowired
    public PlantWikiController(PlantWikiService plantWikiService, PlantService plantService, GardenerFormService gardenerFormService, GardenService gardenService, ImageService imageService) {
        this.plantWikiService = plantWikiService;
        this.plantService = plantService;
        this.gardenerFormService = gardenerFormService;
        this.gardenService = gardenService;
        this.imageService = imageService;
    }


    /**
     * Retrieve an optional of a gardener using the current authentication. We will always have to
     * check whether the gardener was retrieved in the calling method, so the return type was left as
     * an optional
     *
     * @return An optional of the requested gardener
     */
    public Optional<Gardener> getGardenerFromAuthentication() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUserEmail = authentication.getName();
        return gardenerFormService.findByEmail(currentUserEmail);
    }


    /**
     * The main method to get to the plant wiki page
     * @param model the model which has all the necessary attributes
     * @return the html template that displays all the plant information
     * @throws IOException
     * @throws URISyntaxException
     */
    @GetMapping("/plantWiki")
    public String plantWiki(
            Model model
    ) throws IOException, URISyntaxException {
        logger.info("GET /plantWiki");
        List<WikiPlant> resultPlants = plantWikiService.getPlants("");
        model.addAttribute("resultPlants",resultPlants);

        // need to add to model so that the navbar can populate the dropdown
        Optional<Gardener> gardenerOptional = getGardenerFromAuthentication();
        gardenerOptional.ifPresent(value -> gardener = value);
        List<Garden> gardens = gardenService.getGardensByGardenerId(gardener.getId());
        model.addAttribute("gardens", gardens);

        return "plantWikiTemplate";
    }

    /**
     * The post request when the user searches for the plant information. It queries the API and returns matching plants.
     * If no plants are found it will display an error message
     * @param searchTerm the term that the user entered in the search bar
     * @param model the model which has all the necessary attributes
     * @return the html template that displays all the plant information
     * @throws IOException
     * @throws URISyntaxException
     */
    @PostMapping("/plantWiki")
    public String plantWikiSearch(@RequestParam("searchTerm") String searchTerm, Model model) throws IOException, URISyntaxException {

        logger.info("POST /plantWiki");
        List<WikiPlant> resultPlants = plantWikiService.getPlants(searchTerm);
        model.addAttribute("resultPlants", resultPlants);
        String errorMessage = "No plants were found";
        if(resultPlants.isEmpty()) {
            model.addAttribute("errorMessage", errorMessage);
        }
        model.addAttribute("searchTerm", searchTerm);

        // need to add to model so that the navbar can populate the dropdown
        Optional<Gardener> gardenerOptional = getGardenerFromAuthentication();
        gardenerOptional.ifPresent(value -> gardener = value);
        List<Garden> gardens = gardenService.getGardensByGardenerId(gardener.getId());
        model.addAttribute("gardens", gardens);

        return "plantWikiTemplate";
    }


    @PostMapping("/addPlant")
    public String addPlant(
            @RequestParam("gardenId") Long gardenId,
            @RequestParam("name") String name,
            @RequestParam("count") String count,
            @RequestParam("description") String description,
            @RequestParam("date") String date,
            @RequestParam(value = "isDateInvalid", required = false) boolean isDateInvalid,
            @RequestParam(value = "imageUrl", required = false) String imageUrl,
            @RequestParam(value = "file", required = false) MultipartFile file,
            @RequestParam(value = "isFileUploaded", required = false, defaultValue = "false") boolean isFileUploaded,
            Model model,
            RedirectAttributes redirectAttributes) {

        logger.info("POST /addPlant");
        logger.info("Image URL: " + imageUrl);

        Optional<Garden> gardenOptional = gardenService.getGarden(gardenId);
        if (gardenOptional.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Selected garden does not exist.");
            return "redirect:/plantWiki";
        }

        Garden garden = gardenOptional.get();
        String validatedPlantName = ValidityChecker.validatePlantName(name);
        String validatedPlantCount = ValidityChecker.validatePlantCount(count);
        String validatedPlantDescription = ValidityChecker.validatePlantDescription(description);

        boolean isValid = true;

        if (isDateInvalid) {
            redirectAttributes.addFlashAttribute("dateError", "Date is not in valid format, DD/MM/YYYY");
            isValid = false;
        }

        if (!Objects.equals(name, validatedPlantName)) {
            redirectAttributes.addFlashAttribute("nameError", validatedPlantName);
            isValid = false;
        }

        if (count != null && !Objects.equals(count.replace(",", "."), validatedPlantCount)) {
            redirectAttributes.addFlashAttribute("countError", validatedPlantCount);
            isValid = false;
        }

        if (!Objects.equals(description, validatedPlantDescription)) {
            redirectAttributes.addFlashAttribute("descriptionError", validatedPlantDescription);
            isValid = false;
        }

        if (isFileUploaded && file != null && !file.isEmpty()) {
            Optional<String> uploadMessage = imageService.checkValidImage(file);
            if (uploadMessage.isPresent()) {
                redirectAttributes.addFlashAttribute("uploadError", uploadMessage.get());
                isValid = false;
            }
        }

        if (isValid) {
            Plant plant = new Plant(name, garden);

            if (count != null && !count.trim().isEmpty()) {
                plant.setCount(new BigDecimal(validatedPlantCount).stripTrailingZeros().toPlainString());
            }

            if (description != null && !description.trim().isEmpty()) {
                plant.setDescription(validatedPlantDescription);
            }

            if (date != null) {
                DateTimeFormatter inputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
                DateTimeFormatter outputFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
                LocalDate parsedDate = LocalDate.parse(date, inputFormatter);
                String formattedDate = parsedDate.format(outputFormatter);
                plant.setDatePlanted(formattedDate);
            }

            plantService.addPlant(plant);

            // Set the image based on the file or URL
            if (isFileUploaded && file != null && !file.isEmpty()) {
                imageService.savePlantImage(file, plant);
            } else if (imageUrl != null && !imageUrl.isEmpty()) {
                if (imageUrl.equals("/images/placeholder.jpg")) {
                    plant.setImage("/images/placeholder.jpg");
                } else {
                    String filePath = imageService.downloadImage(imageUrl, plant.getId());
                    plant.setImage(filePath);
                }
            } else {
                // Set the default placeholder image if no image URL or file is provided
                plant.setImage("/images/placeholder.jpg");
            }

            plantService.addPlant(plant);

            redirectAttributes.addFlashAttribute("successMessage", "Plant added successfully!");
            return "redirect:/plantWiki";
        } else {
            redirectAttributes.addFlashAttribute("name", name);
            redirectAttributes.addFlashAttribute("count", count);
            redirectAttributes.addFlashAttribute("description", description);
            redirectAttributes.addFlashAttribute("date", date);
            redirectAttributes.addFlashAttribute("imageUrl", imageUrl);
            redirectAttributes.addFlashAttribute("errorOccurred", "an error has occurred");

            return "redirect:/plantWiki";
        }
    }


}



