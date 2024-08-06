package nz.ac.canterbury.seng302.gardenersgrove.controller;

import jakarta.servlet.http.HttpServletRequest;
import nz.ac.canterbury.seng302.gardenersgrove.entity.Garden;
import nz.ac.canterbury.seng302.gardenersgrove.entity.Gardener;
import nz.ac.canterbury.seng302.gardenersgrove.entity.Tag;
import nz.ac.canterbury.seng302.gardenersgrove.service.GardenService;
import nz.ac.canterbury.seng302.gardenersgrove.util.InputValidationUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import nz.ac.canterbury.seng302.gardenersgrove.service.TagService;
import nz.ac.canterbury.seng302.gardenersgrove.util.ValidityChecker;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static java.lang.Long.parseLong;

/**
 * Controller class that handles all the logic for the browse gardens page
 */
@Controller
public class BrowseGardensController {

    private final GardenService gardenService;
    Logger logger = LoggerFactory.getLogger(BrowseGardensController.class);

    private final TagService tagService;

    private final int pageSize;

    @Autowired
    public BrowseGardensController(GardenService gardenService, TagService tagService) {

        this.gardenService = gardenService;
        this.tagService = tagService;
        this.pageSize = 10;
    }

    /**
     * Handles GET request for the browse gardens page. It gets a Page of Garden objects by
     * specifying the page number and page size.
     *
     * @param pageNoString the page number
     * @param model the model
     * @return the browse gardens html template which contains the user interface for the browse gardens page
     */
    @RequestMapping("/browseGardens")
    public String browseGardens(
            @RequestParam(name="pageNo", defaultValue = "0") String pageNoString,
            @RequestParam(name="tags", required = false) List<String> tags,
            @RequestParam(name="searchTerm", defaultValue = "") String searchTerm,
            @RequestParam(name="pageSize", defaultValue = "10") int pageSize,
            Model model
    ) {
        if(model.containsAttribute("pageNo")) {
            pageNoString = (String) model.getAttribute("pageNo");
        }

        Long tagCount;
        if (tags == null) {
            tagCount = 0L;
            tags = new ArrayList<>();
        } else {
            tagCount = (long) tags.size();
            model.addAttribute("tags", tags);
        }

        int pageNo = ValidityChecker.validatePageNumber(pageNoString);
        Page<Garden> gardensPage = gardenService.getSearchResultsPaginated(pageNo, pageSize, searchTerm, tags, tagCount);
        model.addAttribute("gardensPage", gardensPage);
        int totalPages = gardensPage.getTotalPages();
        if (totalPages > 0) {
            List<Integer> pageNumbers = IntStream.rangeClosed(1, totalPages)
                    .boxed()
                    .collect(Collectors.toList());
            model.addAttribute("pageNumbers", pageNumbers);
        }
        if(!model.containsAttribute("tags") && !model.containsAttribute("allTags")) {
            List<String> allTags = tagService.getAllTagNames();
            if(tags != null) {
                for(String selectedTag: tags) {
                    allTags.remove(selectedTag);
                }
                model.addAttribute("tags", tags);
            }
            model.addAttribute("allTags", allTags);
        }

        return "browseGardensTemplate";
    }

    /**
     * Posts a form response with the search term to search for gardens
     * @param pageNo the page number
     * @param pageSize the page size
     * @param model the model
     *
     * @return the browse gardens html template filled with gardens matching the search term
     */
    @PostMapping("/browseGardens")
    public String sendSearchTerm(
            @RequestParam(name="pageNo", defaultValue = "0") int pageNo,
            @RequestParam(name="pageSize", defaultValue = "10") int pageSize,
            @RequestParam(name="searchTerm") String searchTerm,
            @RequestParam(name="tags", required = false) List<String> tags,
            @ModelAttribute("tags") List<String> flashTags,
            Model model) {
        logger.info("POST /browseGardens");

        Long tagCount;
        if (tags == null) {
            tagCount = 0L;
        } else {
            tagCount = (long) tags.size();
            model.addAttribute("tags", tags);
        }

        Page<Garden> gardensPage = gardenService.getSearchResultsPaginated(pageNo, pageSize, searchTerm, tags, tagCount);
        if (gardensPage.getContent().isEmpty()) {
            model.addAttribute("noSearchResults", "No gardens match your search.");
        }
        model.addAttribute("gardensPage", gardensPage);
        int totalPages = gardensPage.getTotalPages();
        if (totalPages > 0) {
            List<Integer> pageNumbers = IntStream.rangeClosed(1, totalPages)
                    .boxed()
                    .collect(Collectors.toList());
            model.addAttribute("pageNumbers", pageNumbers);
        }
        return "browseGardensTemplate";
    }

    /**
     * POST request for add tag which adds a tag to the list of tags that the user is wanting to filter
     * their browse gardens search by
     *
     * @param pageNo the page number
     * @param tag the tag the user typed in or selected
     * @param tags all the tags the user is filtering by
     * @param model the model
     * @param redirectAttributes attributes used to add to the model of the url it is redirected to
     * @return redirects to the browse gardens page
     */
    @PostMapping("/browseGardens/addTag")
    public String addTag(
            @RequestParam(name="pageNo", defaultValue = "0") String pageNo,
            @RequestParam(name="tag-input") String tag,
            @RequestParam(name="tags", required = false) List<String> tags,
            @RequestParam(name="searchTerm") String searchTerm,
            Model model, RedirectAttributes redirectAttributes
    ) {
        redirectAttributes.addFlashAttribute("pageNo", pageNo);
        if(tags == null) {
            tags = new ArrayList<>();
        }
        List<String> allTags = tagService.getAllTagNames();
        if(allTags.contains(tag)) {
            if(tags.contains(tag)) {  // this checks if the typed in tag is already in the user selected tags
                String errorMessage = "You have already selected " + tag;
                redirectAttributes.addFlashAttribute("tag", tag);
                redirectAttributes.addFlashAttribute("tagValid", errorMessage);
            } else {
                tags.add(tag);
            }
        } else {
            // if the typed in tag does not exist
            String errorMessage = "No tag matching " + tag;
            redirectAttributes.addFlashAttribute("tag", tag);
            redirectAttributes.addFlashAttribute("tagValid", errorMessage);
        }
        // removes the tags from the autocomplete
        for(String selectedTag: tags) {
            allTags.remove(selectedTag);
        }

        redirectAttributes.addFlashAttribute("tags", tags);
        redirectAttributes.addFlashAttribute("allTags", allTags);
        redirectAttributes.addFlashAttribute("searchTerm", searchTerm);

        return "redirect:/browseGardens";
    }
}
