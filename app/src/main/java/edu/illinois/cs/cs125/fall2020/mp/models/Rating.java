package edu.illinois.cs.cs125.fall2020.mp.models;

/**
 * creates a Rating.
 */
public class Rating {
  /**
   * not rated is -1.0.
   */
  public static final double NOT_RATED = -1.0;
  private String id;
  private double rating;

  /**
   * defaul constructor.
   */
  @SuppressWarnings({"unused", "RedundantSuppression"})
  public Rating() {}

  /**
   * creates a rating.
   * @param setId uuid
   * @param setRating rating of course
   */
  public Rating(final String setId, final double setRating) {
    id = setId;
    rating = setRating;
  }

  /**
   * returns UUID.
   * @return return uuid
   */
  public String getId() {
    return id;
  }

  /**
   * returns rating as a double.
   * @return rating
   */
  public double getRating() {
    return rating;
  }
}
