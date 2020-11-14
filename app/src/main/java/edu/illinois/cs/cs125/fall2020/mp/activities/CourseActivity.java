package edu.illinois.cs.cs125.fall2020.mp.activities;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Scanner;

import edu.illinois.cs.cs125.fall2020.mp.R;
import edu.illinois.cs.cs125.fall2020.mp.databinding.ActivityCourseBinding;
import edu.illinois.cs.cs125.fall2020.mp.models.Course;
import edu.illinois.cs.cs125.fall2020.mp.models.Summary;

/**
 *
 */
public class CourseActivity extends AppCompatActivity {
  private ActivityCourseBinding binding;
  private ObjectMapper mapper = new ObjectMapper();
  private Summary alpha;
  private Course beta;
  private final Map<Course, String> courses = new HashMap<>();

    /**
     * creates thing.
     * @param savedInstanceState is cool
     */
  @Override
    protected void onCreate(final @NonNull Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    Intent intent = getIntent();
    binding = DataBindingUtil.setContentView(this, R.layout.activity_course);
    try {
      alpha = mapper.readValue(intent.getStringExtra("COURSE"), Summary.class);
    } catch (JsonProcessingException e) {
      e.printStackTrace();
    }
    String filename = "2020_fall.json";
    String json =
            new Scanner(CourseActivity.class.getResourceAsStream(filename), "UTF-8").useDelimiter("\\A").next();
    try {
      JsonNode nodes = mapper.readTree(json);
      for (Iterator<JsonNode> it = nodes.elements(); it.hasNext(); ) {
        JsonNode node = it.next();
        Course course = mapper.readValue(node.toString(), Course.class);
        courses.put(course, node.toPrettyString());
      }
    } catch (JsonProcessingException e) {
      throw new IllegalStateException(e);
    }
    for (Course key : courses.keySet()) {
      if (key.equals(alpha)) {
        beta = key;
      }
    }
    if (beta.getDescription() == null) {
      throw new IllegalArgumentException();
    }
    if (beta.getTitle() == null) {
      throw new IllegalArgumentException();
    }
    binding.textview.setText(beta.getTitle());
    binding.textview.setText(beta.getDescription());
  }
}
