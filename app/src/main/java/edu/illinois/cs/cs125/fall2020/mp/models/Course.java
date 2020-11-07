package edu.illinois.cs.cs125.fall2020.mp.models;

/**
 */
public class Course extends Summary {
  private String description;

    /**
     * get description.
     *
     * @return description
     */
  public String getDescription() {
    return description; }

    /**
     * Empty Course.
     */
  @SuppressWarnings({"unused", "RedundantSuppression"})
  public Course() { }

    /**
     * Course constructor.
     * @param setYear super
     * @param setSemester super
     * @param setDepartment super
     * @param setNumber super
     * @param setTitle super
     * @param setDescription sets description
     */
  public Course(final String setYear,
                final String setSemester,
                final String setDepartment,
                final String setNumber,
                final String setTitle,
                final String setDescription) {
    super(setYear, setSemester, setDepartment, setNumber, setTitle);
    description = setDescription;
  }
}
