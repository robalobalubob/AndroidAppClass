package edu.illinois.cs.cs125.fall2020.mp.activities;

import android.content.Intent;
import android.os.Bundle;

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
import edu.illinois.cs.cs125.fall2020.mp.models.Summary;
import edu.illinois.cs.cs125.fall2020.mp.network.Client;

/**
 *
 */
public class CourseActivity extends AppCompatActivity
    implements Client.CourseClientCallbacks {
  private ActivityCourseBinding binding;
  private ObjectMapper mapper = new ObjectMapper();
  private Summary alpha;

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
    CourseableApplication application = (CourseableApplication) getApplication();
    application.getCourseClient().getCourse(alpha, this);

  }

  /**
   * display course.
   * @param summary a summary
   * @param course a course
   */
  @Override
  public void courseResponse(final Summary summary, final Course course) {
    binding.textview.setText(course.getDescription());
  }
}
