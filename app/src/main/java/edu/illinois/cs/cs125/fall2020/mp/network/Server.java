package edu.illinois.cs.cs125.fall2020.mp.network;

import androidx.annotation.NonNull;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import edu.illinois.cs.cs125.fall2020.mp.application.CourseableApplication;
import edu.illinois.cs.cs125.fall2020.mp.models.Rating;
import edu.illinois.cs.cs125.fall2020.mp.models.Summary;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Scanner;
import okhttp3.mockwebserver.Dispatcher;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;

/**
 * Development course API server.
 *
 * <p>Normally you would run this server on another machine, which the client would connect to over
 * the internet. For the sake of development, we're running the server right alongside the app on
 * the same device. However, all communication between the course API client and course API server
 * is still done using the HTTP protocol. Meaning that eventually it would be straightforward to
 * move this server to another machine where it could provide data for all course API clients.
 *
 * <p>You will need to add functionality to the server for MP1 and MP2.
 */
public final class Server extends Dispatcher {
  @SuppressWarnings({"unused", "RedundantSuppression"})
  private static final String TAG = Server.class.getSimpleName();

  private final Map<String, String> summaries = new HashMap<>();

  private MockResponse getSummary(@NonNull final String path) {
    String[] parts = path.split("/");
    if (parts.length != 2) {
      return new MockResponse().setResponseCode(HttpURLConnection.HTTP_BAD_REQUEST);
    }
    String summary = summaries.get(parts[0] + "_" + parts[1]);
    if (summary == null) {
      return new MockResponse().setResponseCode(HttpURLConnection.HTTP_NOT_FOUND);
    }
    return new MockResponse().setResponseCode(HttpURLConnection.HTTP_OK).setBody(summary);
  }

  @SuppressWarnings("MismatchedQueryAndUpdateOfCollection")
  private final Map<Summary, String> courses = new HashMap<>();

  private MockResponse getCourse(@NonNull final String path) {
    String[] parts = path.split("/");
    Summary summary = new Summary(parts[0], parts[1], parts[2], parts[3], "");
    String banal = courses.get(summary);
    if (banal == null) {
      return new MockResponse().setResponseCode(HttpURLConnection.HTTP_NOT_FOUND);
    }
    return new MockResponse().setResponseCode(HttpURLConnection.HTTP_OK).setBody(banal);
  }
  private final Map<String, Map<Summary, Rating>> courseRatings = new HashMap<>();
  private final Map<String, Rating> singleRatings = new HashMap<>();

  private MockResponse getRating(@NonNull final String path, @NonNull final RecordedRequest request)
          throws JsonProcessingException {
    mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    String[] split = path.split("/");
    String uuid = "";
    if (split.length <= 2) {
      uuid = split[0];
      if (request.getMethod().equals("GET")) {
        if (singleRatings.get(uuid) == null) {
          Rating rate = new Rating(uuid, Rating.NOT_RATED);
          String json = mapper.writeValueAsString(rate);
          return new MockResponse().setResponseCode(HttpURLConnection.HTTP_OK).setBody(json);
        }
        String json = mapper.writeValueAsString(singleRatings.get(uuid));
        return new MockResponse().setResponseCode(HttpURLConnection.HTTP_OK).setBody(json);
      } else if (request.getMethod().equals("POST")) {
        String json = request.getBody().readUtf8();
        Rating rating = mapper.readValue(json, Rating.class);
        if (!rating.getId().equals(uuid)) {
          System.out.println(rating.getId());
          System.out.println("not matching uuid");
          return new MockResponse().setResponseCode(HttpURLConnection.HTTP_BAD_REQUEST);
        } else if (rating == null) {
          return new MockResponse().setResponseCode(HttpURLConnection.HTTP_NOT_FOUND);
        } else {
          return new MockResponse().setResponseCode(HttpURLConnection.HTTP_MOVED_TEMP).setHeader(
                  "Location", "/rating/" + path
          );
        }
      }
    }
    if (split.length > 2) {
      try {
        String[] splice = split[3].split("=");
        uuid = splice[1];
      } catch (ArrayIndexOutOfBoundsException e) {
        return new MockResponse().setResponseCode(HttpURLConnection.HTTP_BAD_REQUEST);
      }
      String[] splice = split[3].split("=");
      uuid = splice[1];
      Summary summary = new Summary(split[0], split[1], split[2], split[3].substring(0, 3), "");
      System.out.println(summary.getNumber());
      String banal = courses.get(summary);
      if (banal == null) {
        return new MockResponse().setResponseCode(HttpURLConnection.HTTP_NOT_FOUND);
      }
    } else {
      uuid = split[0];
    }
    System.out.println(uuid);
    if (uuid == "") {
      return new MockResponse().setResponseCode(HttpURLConnection.HTTP_BAD_REQUEST);
    }
    Summary summary = new Summary(split[0], split[1], split[2], split[3].substring(0, 3), "");
    Map<Summary, Rating> ratings = courseRatings.getOrDefault(uuid, new HashMap<>());
    System.out.println("request method");
    if (request.getMethod().equals("GET")) {
      if (courseRatings.getOrDefault(uuid, new HashMap<>()).get(summary) == null) {
        Rating rate = new Rating(uuid, Rating.NOT_RATED);
        String json = mapper.writeValueAsString(rate);
        ratings.put(summary, rate);
        courseRatings.put(uuid, ratings);
        return new MockResponse().setResponseCode(HttpURLConnection.HTTP_OK).setBody(json);
      }
      String json = mapper.writeValueAsString(courseRatings.get(uuid).get(summary));
      return new MockResponse().setResponseCode(HttpURLConnection.HTTP_OK).setBody(json);
    } else if (request.getMethod().equals("POST")) {
      String json = request.getBody().readUtf8();
      Rating rating = mapper.readValue(json, Rating.class);
      if (!rating.getId().equals(uuid)) {
        System.out.println(rating.getId());
        System.out.println("not matching uuid");
        return new MockResponse().setResponseCode(HttpURLConnection.HTTP_BAD_REQUEST);
      } else if (rating == null) {
        return new MockResponse().setResponseCode(HttpURLConnection.HTTP_NOT_FOUND);
      } else {
        ratings.put(summary, rating);
        courseRatings.put(uuid, ratings);
        return new MockResponse().setResponseCode(HttpURLConnection.HTTP_MOVED_TEMP).setHeader(
                "Location", "/rating/" + path
        );
      }
    }
    return new MockResponse().setResponseCode(HttpURLConnection.HTTP_BAD_REQUEST);
  }
  @NonNull
  @Override
  public MockResponse dispatch(@NonNull final RecordedRequest request) {
    try {
      String path = request.getPath();
      if (path == null || request.getMethod() == null) {
        return new MockResponse().setResponseCode(HttpURLConnection.HTTP_NOT_FOUND);
      } else if (path.equals("/") && request.getMethod().equalsIgnoreCase("HEAD")) {
        return new MockResponse().setResponseCode(HttpURLConnection.HTTP_OK);
      } else if (path.startsWith("/summary/")) {
        return getSummary(path.replaceFirst("/summary/", ""));
      } else if (path.startsWith("/course/")) {
        return getCourse(path.replaceFirst("/course/", ""));
      } else if (path.startsWith("/rating/")) {
        return getRating(path.replaceFirst("/rating/", ""), request);
      }
      return new MockResponse().setResponseCode(HttpURLConnection.HTTP_NOT_FOUND);
    } catch (Exception e) {
      return new MockResponse().setResponseCode(HttpURLConnection.HTTP_INTERNAL_ERROR);
    }
  }

  private static boolean started = false;

  /**
   * Start the server if has not already been started.
   *
   * <p>We start the server in a new thread so that it operates separately from and does not
   * interfere with the rest of the app.
   */
  public static void start() {
    if (!started) {
      new Thread(Server::new).start();
      started = true;
    }
  }

  private final ObjectMapper mapper = new ObjectMapper();

  private Server() {
    mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    loadSummary("2020", "fall");
    loadCourses("2020", "fall");

    try {
      MockWebServer server = new MockWebServer();
      server.setDispatcher(this);
      server.start(CourseableApplication.SERVER_PORT);

      String baseUrl = server.url("").toString();
      if (!CourseableApplication.SERVER_URL.equals(baseUrl)) {
        throw new IllegalStateException("Bad server URL: " + baseUrl);
      }
    } catch (IOException e) {
      throw new IllegalStateException(e.getMessage());
    }
  }

  @SuppressWarnings("SameParameterValue")
  private void loadSummary(@NonNull final String year, @NonNull final String semester) {
    String filename = "/" + year + "_" + semester + "_summary.json";
    String json =
        new Scanner(Server.class.getResourceAsStream(filename), "UTF-8").useDelimiter("\\A").next();
    summaries.put(year + "_" + semester, json);
  }

  @SuppressWarnings("SameParameterValue")
  private void loadCourses(@NonNull final String year, @NonNull final String semester) {
    String filename = "/" + year + "_" + semester + ".json";
    String json =
        new Scanner(Server.class.getResourceAsStream(filename), "UTF-8").useDelimiter("\\A").next();
    try {
      JsonNode nodes = mapper.readTree(json);
      for (Iterator<JsonNode> it = nodes.elements(); it.hasNext(); ) {
        JsonNode node = it.next();
        Summary course = mapper.readValue(node.toString(), Summary.class);
        courses.put(course, node.toPrettyString());
      }
    } catch (JsonProcessingException e) {
      throw new IllegalStateException(e);
    }
  }
}
