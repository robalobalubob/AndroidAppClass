package edu.illinois.cs.cs125.fall2020.mp.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.RatingBar;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import edu.illinois.cs.cs125.fall2020.mp.R;
import edu.illinois.cs.cs125.fall2020.mp.application.CourseableApplication;
import edu.illinois.cs.cs125.fall2020.mp.databinding.ActivityCourseBinding;
import edu.illinois.cs.cs125.fall2020.mp.models.Course;
import edu.illinois.cs.cs125.fall2020.mp.models.Rating;
import edu.illinois.cs.cs125.fall2020.mp.models.Summary;
import edu.illinois.cs.cs125.fall2020.mp.network.Client;

/**
 *
 */
public class CourseActivity extends AppCompatActivity
    implements Client.CourseClientCallbacks, RatingBar.OnRatingBarChangeListener {
  private ActivityCourseBinding binding;
  private ObjectMapper mapper = new ObjectMapper();
  private Summary alpha;
  private Rating rated;
  private Client client;
  private CourseableApplication application;

    /**
     * creates thing.
     * @param savedInstanceState is cool
     */
  @Override
    protected void onCreate(final @NonNull Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    Intent intent = getIntent();
    binding = DataBindingUtil.setContentView(this, R.layout.activity_course);
    try {
      alpha = mapper.readValue(intent.getStringExtra("COURSE"), Summary.class);
    } catch (JsonProcessingException e) {
      e.printStackTrace();
    }
    application = (CourseableApplication) getApplication();
    client = application.getCourseClient();
    application.getCourseClient().getCourse(alpha, this);
    application.getCourseClient().getRating(alpha, application.getClientID(), this);
    binding.rating.setOnRatingBarChangeListener(this);
  }

  /**
   * display course.
   * @param summary a summary
   * @param course a course
   */
  @Override
  public void courseResponse(final Summary summary, final Course course) {
    String title = course.getDepartment() + " " + course.getNumber() + " " + course.getTitle();
    binding.texttitle.setText(title);
    binding.textview.setText(course.getDescription());
  }

  /**
   * get rating.
   * @param summary course
   * @param rating rating assigned to course
   */
  @Override
  public void yourRating(final Summary summary, final Rating rating) {
    System.out.println(rating.getRating());
    binding.rating.setRating((float) rating.getRating());
  }

  /**
   * it worked.
   * @param ratingBar bar of rates
   * @param v no clue
   * @param b nope
   */
  @Override
  public void onRatingChanged(final RatingBar ratingBar, final float v, final boolean b) {
    rated = new Rating(application.getClientID(), ratingBar.getRating());
    application.getCourseClient().postRating(alpha, rated, this);
  }
}
