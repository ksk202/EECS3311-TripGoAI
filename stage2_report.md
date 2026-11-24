# TripGoAI — Stage 2 Implementation Report

## 1. Project Overview and Implementation Status

**TripGoAI** is a Java-based AI travel planning application developed for EECS 3311 at York University. It helps users organize personalized trips based on their destination, travel dates, budget, interests, and travel preferences.

The application supports a graphical user interface (GUI), an interactive command-line interface (CLI), scripted input from text files, and HTTP API requests.

TripGoAI integrates OpenAI's large language model through the `LLMClient` interface and `OpenAIAdapter` class. The AI agent supports natural-language interpretation, itinerary generation, attraction recommendations, itinerary modifications, and packing list generation.

External tools support destination research, weather forecasting, attraction retrieval, restaurant searches, accommodation searches, and transportation routing.

The application also implements preference memory, budget estimation, schedule validation, trip persistence, and reversible itinerary editing.

### Current Implementation Status

The following functionality has been implemented and demonstrated:

- Travel profile and preference management.
- Natural-language request interpretation.
- Destination research.
- Multi-day itinerary generation.
- Attraction recommendations.
- AI-assisted restaurant and accommodation recommendations.
- Driving-route planning.
- Budget estimation and schedule validation.
- Weather forecasts and itinerary reviews.
- Natural-language itinerary editing and Undo.
- Alternative trip planning.
- Packing list generation.
- Trip saving, loading, and deletion.
- Standalone CLI execution.
- File-based input.
- Local HTTP API input.

The application was exported as `TripGoAI.jar` and successfully executed outside Eclipse.

### Known Limitations

Transportation planning currently supports driving only. Some activity prices, opening hours, durations, and travel times may be incomplete or unverified.

Restaurant and accommodation retrieval-tool integration requires further end-to-end verification. Weather-related recommendations are available, but automatic activity replacement has not been fully verified.

AI-dependent functionality requires internet access and a valid OpenAI API key.

---

# 2. Feature-to-Implementation Mapping

This section documents the original Stage 1 requirements, implemented Java classes, design patterns, observed functionality, changes from the original design, and demonstration evidence.

## F01 — Travel Profile and Preferences

**Original Stage 1 Requirement:** Allow travelers to create and manage their travel preferences, including food, transportation, and budget preferences. The application should remember these preferences for later planning.

**Implementation:** The `TravelPlannerGUI.java` and `CommandLineInterface.java` classes receive user preferences. `TravelAgentController.java` coordinates preference operations, while `MemoryManager.java` stores and retrieves the information. `TravelProfile.java` represents traveler information.

**Key Methods:** `savePreferences()`, `savePreference()`, `getPreferences()`.

**Design Patterns:** Singleton and Facade.

**Changes and Rationale:** The original requirement was preserved. Preference management uses a shared memory component to maintain consistency across application operations.

**Testing:** Successfully saved the preference `food = Italian` and retrieved the value using the standalone CLI and `task.txt`.

**Screenshot:** Insert screenshot showing the saved and retrieved preference.

**Demonstration:** https://www.youtube.com/watch?v=IbsQ1SF9MKA

## F02 — Natural-Language Travel Request Interpretation

**Original Stage 1 Requirement:** Allow travelers to describe their desired trip using natural language and automatically extract relevant planning requirements.

**Implementation:** `TravelPlannerGUI.java` and `CommandLineInterface.java` receive the request. `TravelAgentController.java` forwards it to `TravelAgent.java`, which uses `LLMClient.java` and `OpenAIAdapter.java` to interpret the request. `TripRequest.java` represents the extracted requirements.

**Key Methods:** `interpretRequest()`, `interpretTripRequest()`, `generate()`.

**Design Patterns:** Facade and Adapter.

**Changes and Rationale:** The implementation uses the OpenAI API through an adapter to separate provider-specific communication from application logic.

**Screenshot:** Insert screenshot showing an interpreted natural-language travel request.

**Demonstration:** https://www.youtube.com/watch?v=zUhHfCq8wIc

## F03 — Destination Research

**Original Stage 1 Requirement:** Allow users to research destinations and obtain information relevant to trip planning.

**Implementation:** `TravelAgentController.java` coordinates destination research through `TravelAgent.java`, `ToolManager.java`, and `DestinationSearchTool.java`. The GUI and CLI provide access to the research functionality.

**Key Methods:** `researchDestination()` and destination-search operations.

**Design Pattern:** Facade.

**Changes and Rationale:** The feature was implemented through a dedicated search tool to separate external information retrieval from user-interface logic.

**Screenshot:** Insert screenshot showing destination research results.

**Demonstration:** https://www.youtube.com/watch?v=j2muNB-y1k0

## F04 — Multi-Day Itinerary Generation

**Original Stage 1 Requirement:** Generate personalized multi-day travel itineraries based on destination, dates, budget, interests, and planning preferences.

**Implementation:** `TravelPlannerGUI.java` and `CommandLineInterface.java` collect trip information. `TravelAgentController.java` coordinates planning with `TravelAgent.java` and `TripPlanner.java`. `BudgetStrategy.java` and `BalancedStrategy.java` provide different planning priorities. `ScheduleManager.java` and `BudgetManager.java` validate the generated plan.

**Key Methods:** `generateItinerary()`, `generateItineraryAutomatically()`, `planTripWithActivities()`, `checkConflicts()`.

**Design Patterns:** Strategy and Facade.

**Changes and Rationale:** Stage 1 proposed several strategies, including relaxed-pace planning. The implemented application focuses on Budget and Balanced strategies.

**Screenshot:** Insert screenshot showing a generated multi-day itinerary.

**Demonstration:** https://www.youtube.com/watch?v=7xS2FCgBvjY

## F05 — Attraction Recommendations

**Original Stage 1 Requirement:** Recommend attractions based on the traveler's destination and interests.

**Implementation:** `AttractionSearchTool.java` retrieves attraction candidates. `ToolManager.java` manages retrieval tools, `TravelAgent.java` supports AI-assisted evaluation, and `TripPlanner.java` includes selected attractions in the itinerary.

**Key Methods:** `getActivityCandidates()` and attraction-selection operations.

**Design Patterns:** Facade and Strategy through planning integration.

**Changes and Rationale:** Attraction recommendations were integrated into itinerary generation rather than provided only as a separate recommendation workflow. This allows retrieved attractions to be reused during planning.

**Screenshot:** Insert screenshot showing recommended attractions within a generated itinerary.

**Demonstration:** https://www.youtube.com/watch?v=7xS2FCgBvjY

## F06 — Restaurant Recommendations

**Original Stage 1 Requirement:** Recommend restaurants based on destination, cuisine preferences, and budget.

**Implementation:** `RestaurantSearchTool.java` provides restaurant retrieval functionality. `ToolManager.java` manages tool access, while `TravelAgent.java` supports AI-assisted recommendations. Users can request restaurant suggestions through `CommandLineInterface.java`.

**Design Patterns:** Facade and Adapter.

**Changes and Rationale:** A dedicated restaurant-search component was implemented, but complete end-to-end integration of the retrieval tool remains unverified. AI-assisted recommendations can be demonstrated separately.

**Screenshot:** Insert screenshot showing restaurant recommendations.

**Demonstration:** https://www.youtube.com/watch?v=3TOSGm8SLlw

## F07 — Accommodation Planning

**Original Stage 1 Requirement:** Recommend suitable accommodations according to destination, budget, and traveler preferences.

**Implementation:** `AccommodationSearchTool.java` supports accommodation retrieval, `ToolManager.java` manages the tool, and `TravelAgent.java` supports AI-assisted accommodation recommendations.

**Design Patterns:** Facade and Adapter.

**Changes and Rationale:** The accommodation component was implemented separately from the main itinerary workflow. End-to-end retrieval-tool integration requires additional verification.

**Screenshot:** Insert screenshot showing accommodation recommendations.

**Demonstration:** https://www.youtube.com/watch?v=Q6JNlR8QWXs

## F08 — Transportation Planning

**Original Stage 1 Requirement:** Help travelers plan transportation between locations.

**Implementation:** `TravelPlannerGUI.java` collects the origin and destination. `TravelAgentController.java` coordinates the operation, and `TransportationTool.java` uses OSRM and OpenStreetMap to calculate driving routes.

**Key Methods:** `planTransportationRoute()` and transportation-tool operations.

**Design Pattern:** Facade.

**Changes and Rationale:** The original design considered different transportation preferences. The implemented version focuses on driving because this mode is supported by the integrated routing service.

**Screenshot:** Insert screenshot showing the origin, destination, distance, and estimated duration.

**Demonstration:** https://www.youtube.com/watch?v=Axwdm_TbkFM

## F09 — Budget Estimation and Tracking

**Original Stage 1 Requirement:** Calculate travel expenses, compare estimated costs with the available budget, and report the remaining balance.

**Implementation:** `BudgetManager.java` calculates costs using information stored in `Activity.java` and `Itinerary.java`. `TravelPlannerGUI.java` displays the results.

**Key Methods:** `calculateTotal()`, `getBudgetReport()`, `auditPrices()`.

**Changes and Rationale:** Additional handling was introduced for activities with unknown or estimated prices to avoid presenting incomplete cost data as fully verified.

**Screenshot:** Insert screenshot showing the budget report and remaining balance.

**Demonstration:** https://www.youtube.com/watch?v=0KZXTJO72wo

## F10 — Schedule Conflict Detection

**Original Stage 1 Requirement:** Detect overlapping activities and scheduling conflicts within generated itineraries.

**Implementation:** `ScheduleManager.java` validates the scheduled activities stored in `Itinerary.java` and `Activity.java`. `TravelAgentController.java` coordinates the operation.

**Key Methods:** `checkConflicts()`, `getConflictDetails()`, `hasIncompleteSchedule()`.

**Design Pattern:** Facade at the controller level.

**Changes and Rationale:** The original requirement was preserved, with support for identifying incomplete scheduling information.

**Screenshot:** Insert screenshot showing the schedule validation report.

**Demonstration:** https://www.youtube.com/watch?v=0KZXTJO72wo

## F11 — Weather-Aware Itinerary Adjustment

**Original Stage 1 Requirement:** Retrieve weather forecasts and adjust travel activities when weather conditions affect the itinerary.

**Implementation:** `WeatherTool.java` retrieves forecast information, while `WeatherAdjustmentService.java` reviews itinerary activities. `TravelAgentController.java` coordinates the operation.

**Key Methods:** `getWeatherAdvice()`, `reviewItineraryForWeather()`.

**Design Pattern:** Facade.

**Changes and Rationale:** Forecast retrieval and itinerary weather review have been implemented. Automatic replacement of weather-affected activities has not been fully verified.

**Screenshot:** Insert screenshot showing a weather forecast and itinerary weather review.

**Demonstration:** https://www.youtube.com/watch?v=YQro9cwi-44

## F12 — Natural-Language Itinerary Editing

**Original Stage 1 Requirement:** Allow users to modify their itineraries using natural-language instructions and reverse changes when necessary.

**Implementation:** `TravelAgent.java` interprets editing requests. `ItineraryCommandFactory.java` creates commands implementing `ItineraryCommand.java`, which supports execution and reversal. `Itinerary.java` stores the affected activities.

**Key Methods:** `modifyItinerary()`, `modifyTrip()`, `createCommand()`, `execute()`, `undo()`, `undoLastEdit()`.

**Design Patterns:** Command and Facade.

**Changes and Rationale:** Additional validation was introduced to improve activity identification, prevent unintended modifications, and support reliable Undo operations.

**Screenshot:** Insert screenshots showing an itinerary modification and the Undo result.

**Demonstration:** https://youtu.be/y5PGhqNlcTM

## F13 — Alternative Plan Generation

**Original Stage 1 Requirement:** Generate alternative activities or itineraries when the traveler requests a different plan.

**Implementation:** `AlternativePlanService.java` manages alternative generation. `AttractionSearchTool.java` supplies candidates, `TravelAgent.java` supports AI-assisted selection, and `ScheduleManager.java` and `BudgetManager.java` help validate results.

**Key Methods:** `generateAlternative()`, `generateAlternativeReport()`, `rankCandidates()`.

**Design Pattern:** Facade through controller integration.

**Changes and Rationale:** Alternative planning was separated into a dedicated service to improve modularity and preserve the original itinerary.

**Screenshot:** Insert screenshot showing a generated alternative plan.

**Demonstration:** https://www.youtube.com/watch?v=BSxuUIRypv0

## F14 — Packing List Generation

**Original Stage 1 Requirement:** Generate personalized packing recommendations based on destination, travel dates, and preferences.

**Implementation:** `TravelPlannerGUI.java` receives trip information, `TravelAgentController.java` coordinates the request, and `TravelAgent.java` uses `LLMClient.java` and `OpenAIAdapter.java` to generate packing recommendations.

**Key Methods:** `generatePackingList()`, `generate()`.

**Design Patterns:** Facade and Adapter.

**Changes and Rationale:** The original requirement was implemented using AI-assisted generation.

**Screenshot:** Insert screenshot showing a generated packing list.

**Demonstration:** https://youtu.be/YgNR-uhHJWw

## NEW-01 — Trip Persistence and Management

**Requirement:** Allow users to save, load, and delete trips.

**Implementation:** `TravelPlannerGUI.java` and `CommandLineInterface.java` provide trip-management operations. `TravelAgentController.java` coordinates saving and loading, while `TripRepository.java` manages storage. `Trip.java` and `Itinerary.java` represent the saved trip information.

**Design Patterns:** Facade and Repository/DAO.

**Changes and Rationale:** Trip persistence was implemented as additional Stage 2 user-facing functionality.

**Screenshot:** Insert screenshot showing a trip saved and subsequently loaded.

**Demonstration:** https://youtu.be/_ybrPhysXjA

## NEW-02 — Standalone CLI, File Input, and HTTP API

**Original Requirement:** Provide a command-line interface capable of accepting input through a file, standard input, an API call, or another supported mechanism.

**Implementation:** The `Main.java` class processes command-line arguments and initializes the travel agent. The existing `CommandLineInterface.java` supports standard input and scripted menu commands. A new `ApiServer.java` class was created to accept HTTP requests.

**Participating Classes:**

- `Main.java`
- `CommandLineInterface.java`
- `ApiServer.java`
- `TravelAgentController.java`
- `module-info.java`

**Key Methods:** `main()`, `runCLIFromFile()`, `ApiServer.start()`, `askAI()`.

**Design Pattern:** Facade through `TravelAgentController`.

### Standard Input

The user can launch the CLI directly with:

`java -jar TripGoAI.jar -cli`

The application then accepts keyboard input through its command-line menu.

### File Input

The command:

`java -jar TripGoAI.jar -cli -input task.txt`

allows the application to read scripted commands from a text file.

The feature was tested using a file that saved `food = Italian`, retrieved the preference, and exited successfully.

### HTTP API Input

The command:

`java -jar TripGoAI.jar -api`

starts the new local API server.

`ApiServer.java` uses Java's built-in HTTP server and listens on `127.0.0.1:8080` by default.

Two endpoints are provided:

- `GET /health` checks whether the server is running.
- `POST /task` receives plain-text travel questions and forwards them to `TravelAgentController.askAI()`.

The API returns the resulting travel recommendation as an HTTP response.

### Changes and Rationale

The original interactive GUI/CLI selection was preserved when no command-line arguments are supplied.

Additional launch options were introduced to make TripGoAI usable independently of Eclipse and to support different input methods.

The HTTP API was implemented as a separate class to reuse the existing controller and avoid duplicating AI-processing logic.

### Testing Results

The standalone JAR was launched outside Eclipse using Java 25.

The file-input test successfully saved and retrieved a travel preference.

The API health check returned:

`TripGoAI API running`

An HTTP POST request containing a question about attractions in Rome returned travel recommendations.

An initial request failed because `OPENAI_API_KEY` was not configured. After configuring the environment variable, the API returned a response.

**Screenshots to Insert:**

- Screenshot of standalone file-input execution.
- Screenshot of the HTTP API server starting.
- Screenshot of the HTTP POST request and travel recommendations.

---

# 3. Implemented Design Patterns

TripGoAI implements five principal object-oriented design patterns: Facade, Strategy, Adapter, Singleton, and Command.

## 3.1 Facade Pattern

**Design Problem:** The GUI and CLI must access multiple application services without directly managing the complete AI, planning, validation, and persistence workflows.

**Participating Classes:**

- `TravelAgentController` — Facade.
- `TravelPlannerGUI` and `CommandLineInterface` — Clients.
- `TravelAgent`, `TripPlanner`, `BudgetManager`, `ScheduleManager`, and `TripRepository` — Specialized components.

**Implementation:** The user interfaces interact with `TravelAgentController`, which coordinates the underlying services. This reduces coupling between the interfaces and application logic.

The HTTP API also reuses the controller instead of implementing a separate AI-processing workflow.

**Stage 1 Comparison:** The Facade pattern was retained, and its use was extended to support API requests.

## 3.2 Strategy Pattern

**Design Problem:** The application must support multiple itinerary-planning priorities without rewriting the entire planning process.

**Participating Classes:**

- `PlanningStrategy`
- `BudgetStrategy`
- `BalancedStrategy`
- `TripPlanner`
- `TravelAgent`

**Implementation:** `TripPlanner` uses the selected planning strategy to generate an itinerary. `BudgetStrategy` emphasizes lower costs, while `BalancedStrategy` balances activities and planning priorities.

**Stage 1 Comparison:** The implemented version focuses on Budget and Balanced strategies rather than every originally proposed planning variation.

## 3.3 Adapter Pattern

**Design Problem:** The AI agent should communicate with an external language model without directly depending on the provider's implementation details.

**Participating Classes:**

- `LLMClient`
- `OpenAIAdapter`
- `TravelAgent`
- OpenAI API

**Implementation:** `TravelAgent` calls the `LLMClient` interface. `OpenAIAdapter` handles communication with the external OpenAI API.

This architecture allows the provider-specific implementation to be changed without rewriting the travel-agent logic.

**Stage 1 Comparison:** The original Adapter design objective was preserved.

## 3.4 Singleton Pattern

**Design Problem:** Travel preferences must remain consistent across application operations.

**Participating Classes:**

- `MemoryManager`
- `TravelAgent`
- `TravelAgentController`

**Implementation:** `MemoryManager.getInstance()` provides shared access to the application's preference memory.

**Stage 1 Comparison:** The original Singleton design was preserved.

## 3.5 Command Pattern

**Design Problem:** Natural-language itinerary modifications must be executable, validated, and reversible.

**Participating Classes:**

- `ItineraryCommand`
- `ItineraryCommandFactory`
- Concrete editing commands
- `TravelAgent`
- `Itinerary`

**Implementation:** The agent interprets an editing instruction, creates the appropriate command, executes the modification, and supports Undo when required.

**Stage 1 Comparison:** The Command pattern was retained, with additional validation and improved rollback behavior.

---

# 4. Changes and Rationale

Stage 2 preserved the main objectives and 14 features proposed in Stage 1 while introducing implementation refinements.

| Original Design | Implemented Change | Reason |
|---|---|---|
| Multiple itinerary strategies | Budget and Balanced strategies | Focus on working planning options |
| Standalone attraction recommendations | Integrated attraction selection into itinerary generation | Reuse retrieved candidates |
| General transportation planning | Driving-route implementation | Match supported routing functionality |
| Budget estimation | Unknown-price handling | Improve cost transparency |
| Weather-aware adjustment | Forecast retrieval and itinerary review | Support available forecast functionality |
| Natural-language editing | Additional validation and Undo | Prevent unintended changes |
| Alternative itinerary planning | Dedicated `AlternativePlanService` | Improve modularity |
| AI integration | `LLMClient` and `OpenAIAdapter` | Reduce coupling |
| Trip persistence | Repository-based storage | Separate persistence from interfaces |
| CLI input | Added file-input support | Enable scripted execution |
| API input | Added `ApiServer.java` | Support HTTP task submission |
| Application launch | Added `-cli`, `-input`, and `-api` arguments | Support standalone execution |

## 4.1 Design Pattern Changes

The five main patterns described in Section 3 remain implemented.

The Facade pattern was extended through API integration. The Strategy pattern uses the supported Budget and Balanced strategies. The Adapter, Singleton, and Command patterns remain consistent with their original design objectives.

## 4.2 New Components and Functionality

Additional implementation components include:

- `AlternativePlanService.java`
- `WeatherAdjustmentService.java`
- `ApiServer.java`
- Command-line argument handling in `Main.java`
- File-based CLI input
- Standalone JAR execution
- HTTP task submission

## 4.3 Overall Impact

The Stage 2 changes improved modularity, reliability, and the ability to execute TripGoAI outside Eclipse.

The original architecture was largely preserved while new input methods and implementation-specific refinements were introduced.

Stage 1 UML diagrams remain part of the project documentation. Any Stage 2 diagram updates must be verified against the final submitted source.

---


# 6. Development and Verification Summary

TripGoAI was tested through its GUI, CLI, standalone JAR, and HTTP API.

| Feature | Verification | Result |
|---|---|---|
| GUI and CLI | Launched interfaces and performed operations | Working |
| Preference management | Saved and retrieved preferences | Passed |
| Natural-language interpretation | Submitted AI requests | Passed |
| Itinerary generation | Generated multi-day schedules | Passed |
| Attraction recommendations | Retrieved and selected attractions | Passed within planning workflow |
| Budget management | Calculated costs and remaining balance | Passed |
| Schedule validation | Checked schedules | Passed for observed scenarios |
| Driving routes | Tested transportation routing | Passed |
| Weather integration | Retrieved forecasts and reviewed itineraries | Demonstrated |
| Itinerary editing | Modified activities and tested Undo | Passed in observed scenarios |
| Alternative planning | Generated alternatives | Passed |
| Packing lists | Generated recommendations | Passed |
| Trip persistence | Saved and loaded trips | Passed in observed scenarios |
| Standalone JAR | Executed outside Eclipse | Passed |
| Text-file input | Executed `task.txt` | Passed |
| API health check | Requested `/health` | Passed |
| API task input | Submitted travel question to `/task` | Passed |

## 6.1 Standalone CLI Verification

The exported `TripGoAI.jar` was executed using Java 25 outside Eclipse.

The command-line interface successfully processed menu input from a text file.

The test saved the preference `food = Italian`, retrieved the same value, and exited automatically.

## 6.2 HTTP API Verification

The local API server was started using the `-api` command-line argument.

The health-check endpoint successfully returned:

`TripGoAI API running`

A POST request was then submitted to `/task` asking for attractions in Rome.

TripGoAI returned recommendations including the Colosseum, Trevi Fountain, and Vatican-related attractions.

The API was also tested through the exported JAR.

## 6.3 Important Defects Found and Corrected

| Defect | Correction | Result |
|---|---|---|
| Routing did not distinguish origin and destination | Added separate inputs | Corrected route calculation |
| Unsupported transportation modes | Restricted routing to driving | Interface matches implementation |
| Alternative planning could modify unintended attractions | Improved target validation | Reduced unintended changes |
| General alternatives could cause unnecessary replacements | Improved candidate selection | Better preservation of original itinerary |
| Itinerary modifications needed reversal | Implemented command-based Undo | Undo demonstrated |
| Unknown activity prices | Added price-audit handling | Improved transparency |
| OpenAI API key missing in standalone terminal | Configured environment variable | API returned travel recommendations |

## 6.4 Remaining Limitations

- Driving is the currently supported transportation mode.
- Some activity cost and scheduling details may be incomplete.
- Automatic weather-related activity replacement is not fully verified.
- Restaurant and accommodation retrieval integration requires additional verification.
- AI recommendations may require independent accuracy checking.
- AI functionality requires internet access and a valid OpenAI API key.
- File-based CLI input uses scripted menu selections rather than free-form task instructions.
- The HTTP API accepts plain-text travel questions, not every structured trip-management operation.
- The HTTP API is local to the computer running TripGoAI.

---

# 7. Demonstration Index

| Feature | Demonstration Link |
|---|---|
| F01 — Travel Profile and Preferences | https://www.youtube.com/watch?v=IbsQ1SF9MKA |
| F02 — Natural-Language Interpretation | https://www.youtube.com/watch?v=zUhHfCq8wIc |
| F03 — Destination Research | https://www.youtube.com/watch?v=j2muNB-y1k0 |
| F04 — Multi-Day Itinerary Generation | https://www.youtube.com/watch?v=7xS2FCgBvjY |
| F05 — Attraction Recommendations | https://www.youtube.com/watch?v=7xS2FCgBvjY |
| F06 — Restaurant Recommendations | https://www.youtube.com/watch?v=3TOSGm8SLlw |
| F07 — Accommodation Planning | https://www.youtube.com/watch?v=Q6JNlR8QWXs |
| F08 — Transportation Planning | https://www.youtube.com/watch?v=Axwdm_TbkFM |
| F09 — Budget Estimation | https://www.youtube.com/watch?v=0KZXTJO72wo |
| F10 — Schedule Conflict Detection | https://www.youtube.com/watch?v=0KZXTJO72wo |
| F11 — Weather-Aware Planning | https://www.youtube.com/watch?v=YQro9cwi-44 |
| F12 — Itinerary Editing and Undo | https://youtu.be/y5PGhqNlcTM |
| F13 — Alternative Planning | https://www.youtube.com/watch?v=BSxuUIRypv0 |
| F14 — Packing List Generation | https://youtu.be/YgNR-uhHJWw |
| NEW-01 — Trip Persistence | https://youtu.be/_ybrPhysXjA |
| NEW-02 — CLI and API Input | Screenshot evidence pending |

## 7.1 Standalone Input Demonstration

TripGoAI supports three input mechanisms.

**Interactive CLI:**

`java -jar TripGoAI.jar -cli`

**File input:**

`java -jar TripGoAI.jar -cli -input task.txt`

**HTTP API:**

`java -jar TripGoAI.jar -api`

After starting the API server, the following commands can be used to test it:

**Health check:**

`curl.exe http://127.0.0.1:8080/health`

**Travel task:**

`curl.exe -X POST http://127.0.0.1:8080/task -H "Content-Type: text/plain" --data "Suggest three attractions to visit in Rome"`

These commands were tested during development.

## 7.2 Screenshot Evidence

Screenshots should be inserted into the relevant feature sections to show the functionality running.

The required evidence includes:

- Screenshots for each original feature F01–F14.
- Trip persistence demonstration.
- File-input test.
- HTTP API server startup.
- API request and response.
- A demonstration completing the same task through the GUI and CLI.

These screenshots will supplement the video demonstrations and implementation explanations provided in the report.

---

# 8. Conclusion

TripGoAI implements the core AI travel-planning functionality proposed during Stage 1 while introducing additional functionality during Stage 2.

The application combines AI-based planning with external travel tools, preference memory, budget and schedule validation, itinerary editing, and persistent trip management.

Five principal design patterns support the architecture: Facade, Strategy, Adapter, Singleton, and Command.

The standalone executable JAR was successfully tested outside Eclipse. The CLI supports interactive commands and scripted text-file input, while the additional HTTP API supports local submission of travel questions.

These improvements demonstrate that TripGoAI can be operated through multiple input methods while reusing the same underlying travel-agent architecture.

Remaining implementation limitations and verification gaps have been documented for transparency.
