/**
 * Copyright 2017 IBM Corp. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
package com.ibm.watson.tone_analyzer.v3;

import com.ibm.cloud.sdk.core.http.HttpHeaders;
import com.ibm.cloud.sdk.core.http.HttpMediaType;
import com.ibm.cloud.sdk.core.util.RequestUtils;
import com.ibm.watson.common.WatsonServiceUnitTest;
import com.ibm.watson.tone_analyzer.v3.model.ToneAnalysis;
import com.ibm.watson.tone_analyzer.v3.model.ToneChatOptions;
import com.ibm.watson.tone_analyzer.v3.model.ToneOptions;
import com.ibm.watson.tone_analyzer.v3.model.Utterance;
import com.ibm.watson.tone_analyzer.v3.model.UtteranceAnalyses;
import okhttp3.mockwebserver.RecordedRequest;
import org.apache.commons.lang3.StringUtils;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Tone Analyzer unit test.
 */
public class ToneAnalyzerTest extends WatsonServiceUnitTest {

  private static final String VERSION_DATE = "version";
  private static final String FIXTURE = "src/test/resources/tone_analyzer/tone.json";
  private static final String CHAT_FIXTURE = "src/test/resources/tone_analyzer/tone_chat.json";
  private static final String TONE_PATH = "/v3/tone";
  private static final String CHAT_TONE_PATH = "/v3/tone_chat";

  /** The service. */
  private ToneAnalyzer service;
  private static final String VERSION_DATE_VALUE = "2017-09-21";

  /*
   * (non-Javadoc)
   * @see com.ibm.watson.developer_cloud.WatsonServiceTest#setUp()
   */
  @Override
  @Before
  public void setUp() throws Exception {
    super.setUp();
    service = new ToneAnalyzer(VERSION_DATE_VALUE);
    service.setUsernameAndPassword("", "");
    service.setEndPoint(getMockWebServerUrl());

  }

  /**
   * Test README.
   */
  @Test
  public void testReadme() throws InterruptedException, IOException {

    ToneAnalyzer service = new ToneAnalyzer(VERSION_DATE);
    service.setUsernameAndPassword("<username>", "<password>");

    service.setEndPoint(getMockWebServerUrl()); // exclude
    ToneAnalysis mockResponse = loadFixture(FIXTURE, ToneAnalysis.class); // exclude
    server.enqueue(jsonResponse(mockResponse)); // exclude

    String text = "I love these videos "
        + "disappointing for the last two videos "
        + "Watching Youtube is fun! "
        + " I love Youtube! "
        + "Those videos are beautiful to watch. ";

    // Call the service and get the tone
    ToneOptions toneOptions = new ToneOptions.Builder().html(text).build();
    ToneAnalysis tone = service.tone(toneOptions).execute().getResult();
    System.out.println(tone);
  }

  /**
   * Test tone with null.
   */
  @Test(expected = IllegalArgumentException.class)
  public void testtoneWithNull() {
    service.tone(null);
  }

  /**
   * Test get tones.
   *
   * @throws InterruptedException the interrupted exception
   * @throws IOException Signals that an I/O exception has occurred.
   */
  @Test
  public void testtones() throws InterruptedException, IOException {
    String text = " I love these videos "
        + "disappointing for the last two videos "
        + "Watching Youtube is fun! "
        + " I love Youtube! "
        + "Those videos are beautiful to watch. ";

    ToneAnalysis mockResponse = loadFixture(FIXTURE, ToneAnalysis.class);
    server.enqueue(jsonResponse(mockResponse));
    server.enqueue(jsonResponse(mockResponse));
    server.enqueue(jsonResponse(mockResponse));

    // execute request
    ToneOptions toneOptions = new ToneOptions.Builder().html(text).build();
    ToneAnalysis serviceResponse = service.tone(toneOptions).execute().getResult();

    // first request
    RecordedRequest request = server.takeRequest();

    String path = StringUtils.join(TONE_PATH, "?", VERSION_DATE, "=", VERSION_DATE_VALUE);
    assertEquals(path, request.getPath());
    assertNotNull(request.getHeader(HttpHeaders.AUTHORIZATION));
    assertEquals(serviceResponse, mockResponse);
    assertEquals(HttpMediaType.APPLICATION_JSON, request.getHeader(HttpHeaders.ACCEPT));

    // second request
    serviceResponse = service.tone(new ToneOptions.Builder().html(text).build()).execute().getResult();
    request = server.takeRequest();
    assertEquals(path, request.getPath());
    assertTrue(request.getHeader(HttpHeaders.CONTENT_TYPE).startsWith(HttpMediaType.TEXT_HTML));

    // third request
    ToneOptions toneOptions1 = new ToneOptions.Builder()
        .html(text)
        .addTone(ToneOptions.Tone.EMOTION)
        .addTone(ToneOptions.Tone.LANGUAGE)
        .addTone(ToneOptions.Tone.SOCIAL)
        .build();
    serviceResponse = service.tone(toneOptions1).execute().getResult();
    request = server.takeRequest();
    path = path + "&tones=" + RequestUtils.encode("emotion,language,social");
    assertEquals(path, request.getPath());
  }

  /**
   * Test to get Chat tones.
   *
   * @throws InterruptedException the interrupted exception
   * @throws IOException Signals that an I/O exception has occurred.
   */
  @Test
  public void testGetChatTones() throws IOException, InterruptedException {

    String[] users = { "user", "user" };

    String[] texts = {
        "My TV wasn't working.",
      
          + " HHHHHHHHHHHHHHHH. "
          + "HHHHHHHHHHHHHHHHHHHHHHH.",
      "GGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGG"
    };

    List<Utterance> utterances = new ArrayList<>();
    for (int i = 0; i < texts.length; i++) {
      Utterance utterance = new Utterance.Builder()
          .text(texts[i])
          .user(users[i])
          .build();
      utterances.add(utterance);
    }

    ToneChatOptions toneChatOptions = new ToneChatOptions.Builder()
        .utterances(utterances)
        .build();

    UtteranceAnalyses mockResponse = loadFixture(CHAT_FIXTURE, UtteranceAnalyses.class);
    server.enqueue(jsonResponse(mockResponse));
    server.enqueue(jsonResponse(mockResponse));
    server.enqueue(jsonResponse(mockResponse));

    // execute request
    UtteranceAnalyses serviceResponse = service.toneChat(toneChatOptions).execute().getResult();

    // first request
    RecordedRequest request = server.takeRequest();

    String path = StringUtils.join(CHAT_TONE_PATH, "?", VERSION_DATE, "=", VERSION_DATE_VALUE);
    assertEquals(path, request.getPath());
    assertNotNull(request.getHeader(HttpHeaders.AUTHORIZATION));
    assertEquals(serviceResponse, mockResponse);
    assertEquals(HttpMediaType.APPLICATION_JSON, request.getHeader(HttpHeaders.ACCEPT));
  }
}
