package nz.ac.canterbury.seng302.gardenersgrove.util;

import nz.ac.canterbury.seng302.gardenersgrove.entity.Garden;
import nz.ac.canterbury.seng302.gardenersgrove.entity.Tag;
import nz.ac.canterbury.seng302.gardenersgrove.service.TagService;

import java.util.Optional;

/** Utility class for validating and checking the usage of tags. */
public class TagValidation {
  private final TagService tagService;

  public TagValidation(TagService tagService) {
    this.tagService = tagService;
  }

  /**
   * Validates a tag based on its length and character
   *
   * @param tagName the tag name to validate
   * @return an error message if validation fails, or empty if the tag name is valid
   */
  public Optional<String> validateTag(String tagName) {
    String tagRegex = "^[A-Za-zÀ-ÖØ-öø-ž0-9 _\\-'\"]+$";
    if (tagName.length() > 25) {
      return Optional.of("A tag cannot exceed 25 characters");
    } else {
      return tagName.matches(tagRegex)
          ? Optional.empty()
          : Optional.of(
              "The tag name must only contain alphanumeric characters, spaces, -, _, ', or \"");
    }
  }

  /**
   * checks if a tag with the given name is already in use in the specified garden, to prevent
   * duplicates
   *
   * @param tagName the tag name to check
   * @param garden the garden to check the tag against
   * @return "Used" if the tag is in use in the specified garden, or empty if the tag is not found
   *     or not used in the garden
   */

  /**
   * Checks if a tag with the given name is already in use in the specified garden, to prevent
   * duplicates.
   *
   * @param tagName the tag name to check
   * @param garden the garden to check the tag against
   * @return "Used" if the tag is in use in the specified garden, or empty if the tag is not found
   *     or not used in the garden.
   */
  public Optional<String> checkTagInUse(String tagName, Garden garden) {
    Optional<Tag> tagOptional = tagService.findTagByNameAndGarden(tagName, garden);
    if (tagOptional.isPresent()) {
      return Optional.of("Used");
    }
    return Optional.empty();
  }
}
