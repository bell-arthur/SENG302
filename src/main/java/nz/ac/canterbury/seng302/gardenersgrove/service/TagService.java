package nz.ac.canterbury.seng302.gardenersgrove.service;

import nz.ac.canterbury.seng302.gardenersgrove.entity.Garden;
import nz.ac.canterbury.seng302.gardenersgrove.entity.Gardener;
import nz.ac.canterbury.seng302.gardenersgrove.entity.Tag;
import nz.ac.canterbury.seng302.gardenersgrove.repository.TagRepository;
import org.springframework.stereotype.Service;

import java.util.*;

/** Service class for managing Plant entities */
@Service
public class TagService {

  private TagRepository tagRepository;

  private EmailUserService emailUserService;

  /**
   * Constructs a TagService with the TagRepository
   *
   * @param tagRepository is the repo for managing Tags
   */
  public TagService(TagRepository tagRepository) {
    this.tagRepository = tagRepository;
  }

  /**
   * @return a list of all tags
   */
  public List<Tag> getAllTags() {
    return tagRepository.findAll();
  }

  /**
   * @param tag the tag (consisting of string and garden id)
   * @return the added tag
   */
  public Tag addTag(Tag tag) {
    return tagRepository.save(tag);
  }

  /**
   * @param id of the tag
   * @return the tag that matches id
   */
  public Optional<Tag> getTag(Long id) {
    return tagRepository.findById(id);
  }

  public List<String> getTags(Long id) {
    return tagRepository.getTagsByGardenId(id);
  }

  public Optional<Tag> findTagByNameAndGarden(String name, Garden garden) {
    return tagRepository.findByNameAndGarden(name, garden);
  }

  /**
   * Gets a list of all unique tags in the system that do not exist in the specified garden
   *
   * @param id the id of the garden
   * @return a list of all unique tags in the system that do not exist in the specified garden
   */
  public Set<String> getUniqueTagNames(Long id) {
    Set<String> uniqueTagNames = new HashSet<>();
    List<Tag> tags = getAllTags();
    List<String> tagNamesInGarden = getTags(id);
    for(Tag tag : tags) {
      if(!tagNamesInGarden.contains(tag.getName())) {
        uniqueTagNames.add(tag.getName());
      }
    }
    return uniqueTagNames;
  }

  /**
   *  Check the number of bad words that the user tried to use,
   *  if it is 5th time, send warning email and show a relevant message,
   *  if it is 6th time, un-log the user from the system and block the account for 7 days and send a relevant email.
   *  otherwise, show the bad word message to let the user know.
   * @param gardener the gardener of the current user
   * @return bad word warning message
   */
  public String addBadWordCount(Gardener gardener) {

    gardener.setBadWordCount(gardener.getBadWordCount() + 1);

    if (gardener.getBadWordCount() == 5) {
      String emailTitle = "!! Warning for bad words !!";
      String emailMessage = "You have reached the maximum number of bad words on our web site. If you add another inappropriate tag, Your account will be blocked for one week.";
      emailUserService.sendEmail(gardener.getEmail(), emailTitle, emailMessage);
      return "You have added an inappropriate tag for the fifth time, you will receive an warning email.";

    } else if (gardener.getBadWordCount() == 6) {

      //maybe we can do the action to ban the account here(??)

      return "You have added an inappropriate tag for the sixth time, your account will be blocked for one week.";
    }
    return "Submitted tag fails moderation requirements";
  }


}
