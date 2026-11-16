# 1 Project Overview and Implementation Status

**TripGoAI** is a Java-based AI travel planning application designed to help travelers organize personalized trips. It provides both a graphical user interface (GUI) and a command-line interface (CLI), allowing users to plan trips based on their destination, travel dates, budget, interests, and preferences.

The application integrates **OpenAI's large language model (LLM)** through the `LLMClient` interface and `OpenAIAdapter` class to interpret natural-language requests, assist with itinerary generation, recommend attractions, modify travel plans, and generate packing lists. It combines AI capabilities with external tools for destination research, weather forecasts, and transportation routing. The agent also uses memory to store travel preferences and supports multi-step planning.

**Current Working Functionality:** TripGoAI supports travel preference management, natural-language request interpretation, destination research, multi-day itinerary generation, attraction selection, budget estimation, schedule conflict checking, weather forecasts and itinerary reviews, driving-route planning, itinerary modifications with undo, alternative plan generation, packing lists, and trip saving/loading. Restaurant and accommodation recommendation components are also included, although their end-to-end tool integration requires further verification.

**Limitations and Incomplete Functionality:** Transportation routing currently supports driving only. Some attraction prices, opening hours, travel times, and activity durations are unverified or unavailable, so budget estimates may be incomplete. Weather adjustments depend on available forecast data, and automatic replacement of weather-affected activities has not been fully verified. Restaurant and accommodation recommendations are accessible through AI assistance, but direct retrieval-tool execution has not been fully confirmed. The application also requires an internet connection and a valid API key for AI-dependent functionality.

Overall, TripGoAI provides a working GUI and CLI implementation of the core Stage 1 travel planning features, with AI integration, external tool use, preference memory, and deterministic budget and scheduling validation. The remaining limitations are documented for transparency.




# 2 Feature to Implementation Mapping

This section maps all 14 features from the Stage 1 design and the additional Stage 2 functionality to their implementation in TripGoAI. Each entry identifies the participating classes, their responsibilities, relevant methods, design patterns, and demonstration evidence. The implementation explanations describe how the classes collaborate to produce the final result.

## F01  Travel Profile and Preferences

| Mapping field | Entry |
|---|---|
| Feature ID and name | F01  Travel Profile and Preferences |
| Stage 1 feature/reference | F01  Original Stage 1 feature |
| Status or change | Implemented |
| Implemented classes/files and responsibilities | `TravelPlannerGUI.java` and `CommandLineInterface.java` collect preferences; `TravelAgentController.java` coordinates operations; `MemoryManager.java` stores and retrieves preferences; `TravelProfile.java` represents traveler information. |
| Key methods | `savePreferences()`, `savePreference()`, `getPreferences()` |
| Design patterns | Singleton, Facade |
| Video link/timestamp | [https://www.youtube.com/watch?v=IbsQ1SF9MKA] |

**Implementation explanation:** The traveler enters preferences through the GUI or CLI. The controller stores them using the memory component so they can be reused during future planning operations.

**Feature type:** Hybrid.

## F02  Natural-Language Travel Request Interpretation

| Mapping field | Entry |
|---|---|
| Feature ID and name | F02  Natural-Language Travel Request Interpretation |
| Stage 1 feature/reference | F02  Original Stage 1 feature |
| Status or change | Implemented |
| Implemented classes/files and responsibilities | `TravelPlannerGUI.java` and `CommandLineInterface.java` receive requests; `TravelAgentController.java` coordinates interpretation; `TravelAgent.java` uses AI integration; `TripRequest.java` stores extracted requirements; `LLMClient.java` and `OpenAIAdapter.java` connect the agent to the language model. |
| Key methods | `interpretRequest()`, `interpretTripRequest()`, `generate()` |
| Design patterns | Facade, Adapter |
| Video link/timestamp | [https://www.youtube.com/watch?v=zUhHfCq8wIc] |

**Implementation explanation:** The traveler describes a trip in natural language. The controller forwards the request to the travel agent, which uses the LLM to extract destination, dates, budget, interests, and preferences into structured trip information.

**Feature type:** Hybrid.

## F03  Destination Research

| Mapping field | Entry |
|---|---|
| Feature ID and name | F03  Destination Research |
| Stage 1 feature/reference | F03  Original Stage 1 feature |
| Status or change | Implemented |
| Implemented classes/files and responsibilities | `TravelPlannerGUI.java` accepts destination input; `TravelAgentController.java` coordinates research; `TravelAgent.java`, `ToolManager.java`, and `DestinationSearchTool.java` support destination information retrieval. |
| Key methods | `researchDestination()` and destination-search operations |
| Design patterns | Facade |
| Video link/timestamp | [https://www.youtube.com/watch?v=j2muNB-y1k0] |

**Implementation explanation:** The traveler enters a destination. The controller initiates research through the travel agent and retrieval tools. The collected information is returned to the GUI or CLI and can also support itinerary planning.

**Feature type:** Hybrid.

## F04  Multi-Day Itinerary Generation

| Mapping field | Entry |
|---|---|
| Feature ID and name | F04  Multi-Day Itinerary Generation |
| Stage 1 feature/reference | F04  Original Stage 1 feature |
| Status or change | Implemented |
| Implemented classes/files and responsibilities | `TravelPlannerGUI.java` and `CommandLineInterface.java` collect trip details; `TravelAgentController.java` coordinates planning; `TravelAgent.java` supports AI selection; `TripPlanner.java`, `BudgetStrategy.java`, and `BalancedStrategy.java` generate itineraries; `ScheduleManager.java` and `BudgetManager.java` validate results. |
| Key methods | `generateItinerary()`, `generateItineraryAutomatically()`, `planTripWithActivities()`, `checkConflicts()` |
| Design patterns | Strategy, Facade |
| Video link/timestamp | [https://www.youtube.com/watch?v=7xS2FCgBvjY] |

**Implementation explanation:** The traveler provides a destination, dates, interests, budget, and planning strategy. TripGoAI retrieves activity candidates, uses AI-assisted selection, generates a multi-day schedule, and checks activity timing and estimated costs.

**Feature type:** Hybrid.

## F05  Attraction Recommendations

| Mapping field | Entry |
|---|---|
| Feature ID and name | F05  Attraction Recommendations |
| Stage 1 feature/reference | F05  Original Stage 1 feature |
| Status or change | Modified  integrated into itinerary generation |
| Implemented classes/files and responsibilities | `AttractionSearchTool.java` retrieves attraction candidates; `TravelAgent.java` supports AI-based evaluation; `ToolManager.java` manages tool access; `TripPlanner.java` includes selected attractions in the itinerary. |
| Key methods | `getActivityCandidates()` and attraction-selection operations |
| Design patterns | Facade, Strategy through planning integration |
| Video link/timestamp | [https://www.youtube.com/watch?v=7xS2FCgBvjY] |

**Implementation explanation:** TripGoAI retrieves attractions relevant to the destination and traveler interests. The agent helps select suitable candidates, which are then organized into the generated itinerary.

**Feature type:** Hybrid.

## F06  Restaurant Recommendations

| Mapping field | Entry |
|---|---|
| Feature ID and name | F06  Restaurant Recommendations |
| Stage 1 feature/reference | F06  Original Stage 1 feature |
| Status or change | Implemented tool component; end-to-end integration requires verification |
| Implemented classes/files and responsibilities | `RestaurantSearchTool.java` provides restaurant retrieval functionality; `ToolManager.java` manages available tools; `TravelAgent.java` supports AI-assisted restaurant recommendations; `CommandLineInterface.java` provides access to AI requests. |
| Key methods | Restaurant-search operations and `generate()` for AI responses |
| Design patterns | Facade, Adapter for AI assistance |
| Video link/timestamp | [https://www.youtube.com/watch?v=3TOSGm8SLlw] |

**Implementation explanation:** The traveler requests restaurant recommendations based on destination, cuisine, and budget. The application can generate AI-assisted recommendations, while the dedicated restaurant retrieval component is intended to provide supporting restaurant information. End-to-end execution of that component remains to be verified.

**Feature type:** Hybrid.

## F07  Accommodation Planning

| Mapping field | Entry |
|---|---|
| Feature ID and name | F07  Accommodation Planning |
| Stage 1 feature/reference | F07  Original Stage 1 feature |
| Status or change | Implemented tool component; end-to-end integration requires verification |
| Implemented classes/files and responsibilities | `AccommodationSearchTool.java` supports accommodation retrieval; `ToolManager.java` manages tool access; `TravelAgent.java` supports AI-assisted accommodation recommendations; `CommandLineInterface.java` accepts user requests. |
| Key methods | Accommodation-search operations and `generate()` for AI responses |
| Design patterns | Facade, Adapter for AI assistance |
| Video link/timestamp | [https://www.youtube.com/watch?v=Q6JNlR8QWXs] |

**Implementation explanation:** The traveler specifies a destination, accommodation budget, and location preferences. The agent can provide accommodation suggestions through AI assistance, while the dedicated accommodation tool provides a separate retrieval component whose end-to-end use still requires verification.

**Feature type:** Hybrid.

## F08  Transportation Planning

| Mapping field | Entry |
|---|---|
| Feature ID and name | F08  Transportation Planning |
| Stage 1 feature/reference | F08  Original Stage 1 feature |
| Status or change | Modified  driving-route implementation |
| Implemented classes/files and responsibilities | `TravelPlannerGUI.java` collects starting and destination locations; `TravelAgentController.java` coordinates routing; `TransportationTool.java` calculates driving routes using OSRM and OpenStreetMap. |
| Key methods | `planTransportationRoute()` and transportation-tool operations |
| Design patterns | Facade |
| Video link/timestamp | [https://www.youtube.com/watch?v=Axwdm_TbkFM] |

**Implementation explanation:** The traveler enters an origin and destination. TripGoAI uses its transportation tool to obtain a driving route, then displays the estimated distance, duration, and map link.

**Feature type:** Deterministic.

## F09  Budget Estimation and Tracking

| Mapping field | Entry |
|---|---|
| Feature ID and name | F09  Budget Estimation and Tracking |
| Stage 1 feature/reference | F09  Original Stage 1 feature |
| Status or change | Modified  includes unknown-price reporting |
| Implemented classes/files and responsibilities | `BudgetManager.java` calculates costs and generates budget reports; `Activity.java` stores activity-cost information; `Itinerary.java` groups activities; `TravelPlannerGUI.java` displays the calculated budget. |
| Key methods | `calculateTotal()`, `getBudgetReport()`, `auditPrices()` |
| Design patterns | N/A  deterministic budget-management service |
| Video link/timestamp | [https://www.youtube.com/watch?v=0KZXTJO72wo] |

**Implementation explanation:** After an itinerary is generated, `BudgetManager` calculates the known activity expenses, compares them against the traveler's budget, and reports the remaining balance. Unknown costs are identified separately.

**Feature type:** Deterministic.

## F10  Schedule Conflict Detection

| Mapping field | Entry |
|---|---|
| Feature ID and name | F10  Schedule Conflict Detection |
| Stage 1 feature/reference | F10  Original Stage 1 feature |
| Status or change | Implemented |
| Implemented classes/files and responsibilities | `ScheduleManager.java` validates activity schedules; `Itinerary.java` organizes the activities; `Activity.java` stores scheduled dates and times; `TravelAgentController.java` coordinates application operations. |
| Key methods | `checkConflicts()`, `getConflictDetails()`, `hasIncompleteSchedule()` |
| Design patterns | Facade at the controller level |
| Video link/timestamp | [https://www.youtube.com/watch?v=0KZXTJO72wo] |

**Implementation explanation:** TripGoAI checks activity schedules after itinerary generation or modification. The schedule manager examines the planned times and reports overlapping activities or incomplete schedule information.

**Feature type:** Deterministic.

## F11  Weather-Aware Itinerary Adjustment

| Mapping field | Entry |
|---|---|
| Feature ID and name | F11  Weather-Aware Itinerary Adjustment |
| Stage 1 feature/reference | F11  Original Stage 1 feature |
| Status or change | Implemented weather retrieval and review; automatic replacements not fully verified |
| Implemented classes/files and responsibilities | `WeatherTool.java` retrieves forecasts; `WeatherAdjustmentService.java` reviews itinerary activities; `TravelAgentController.java` coordinates requests; `ScheduleManager.java` supports schedule validation. |
| Key methods | `getWeatherAdvice()`, `reviewItineraryForWeather()` |
| Design patterns | Facade |
| Video link/timestamp | [https://www.youtube.com/watch?v=YQro9cwi-44] |

**Implementation explanation:** The traveler requests a weather forecast or itinerary review. TripGoAI retrieves available forecast information and checks whether scheduled activities could be affected. The application reports recommendations or leaves the plan unchanged when no adjustment is needed.

**Feature type:** Hybrid.

## F12  Natural-Language Itinerary Editing

| Mapping field | Entry |
|---|---|
| Feature ID and name | F12  Natural-Language Itinerary Editing |
| Stage 1 feature/reference | F12  Original Stage 1 feature |
| Status or change | Implemented with additional validation and Undo support |
| Implemented classes/files and responsibilities | `TravelPlannerGUI.java` accepts editing instructions; `TravelAgentController.java` coordinates the request; `TravelAgent.java` interprets editing intent; `ItineraryCommandFactory.java` creates editing commands; `ItineraryCommand.java` defines execution and reversal behavior; `Itinerary.java` stores the affected activities. |
| Key methods | `modifyItinerary()`, `modifyTrip()`, `createCommand()`, `execute()`, `undo()`, `undoLastEdit()` |
| Design patterns | Command, Facade |
| Video link/timestamp | [https://youtu.be/y5PGhqNlcTM] |

**Implementation explanation:** The traveler describes an itinerary modification in natural language. The agent interprets the requested change, a command applies it to the appropriate activity, and validation checks the updated itinerary. Undo restores the previous state.

**Feature type:** Hybrid.

## F13  Alternative Plan Generation

| Mapping field | Entry |
|---|---|
| Feature ID and name | F13  Alternative Plan Generation |
| Stage 1 feature/reference | F13  Original Stage 1 feature |
| Status or change | Modified  dedicated alternative-planning service |
| Implemented classes/files and responsibilities | `AlternativePlanService.java` generates alternative plans; `AttractionSearchTool.java` retrieves candidate attractions; `TravelAgent.java` supports AI-assisted selection; `ScheduleManager.java` and `BudgetManager.java` validate resulting plans. |
| Key methods | `generateAlternative()`, `generateAlternativeReport()`, `rankCandidates()` |
| Design patterns | Facade through controller integration |
| Video link/timestamp | [https://www.youtube.com/watch?v=BSxuUIRypv0] |

**Implementation explanation:** The traveler requests an alternative activity or itinerary. TripGoAI identifies possible replacements, uses AI-assisted ranking where appropriate, and creates a separate alternative plan while preserving the original itinerary.

**Feature type:** Hybrid.

## F14  Packing List Generation

| Mapping field | Entry |
|---|---|
| Feature ID and name | F14  Packing List Generation |
| Stage 1 feature/reference | F14  Original Stage 1 feature |
| Status or change | Implemented |
| Implemented classes/files and responsibilities | `TravelPlannerGUI.java` collects trip information; `TravelAgentController.java` coordinates generation; `TripRequest.java` represents travel requirements; `TravelAgent.java`, `LLMClient.java`, and `OpenAIAdapter.java` support AI-generated packing recommendations. |
| Key methods | `generatePackingList()`, `generate()` |
| Design patterns | Facade, Adapter |
| Video link/timestamp | [https://youtu.be/YgNR-uhHJWw] |

**Implementation explanation:** The traveler supplies destination, dates, and travel preferences. TripGoAI sends relevant trip context to the AI integration, which generates a categorized packing list for the traveler.

**Feature type:** AI-based.

## NEW  Trip Persistence and Management

| Mapping field | Entry |
|---|---|
| Feature ID and name | NEW-01  Trip Persistence and Management |
| Stage 1 feature/reference | New  Additional Stage 2 user-facing feature |
| Status or change | Implemented |
| Implemented classes/files and responsibilities | `TravelPlannerGUI.java` and `CommandLineInterface.java` provide trip-management operations; `TravelAgentController.java` coordinates saving and retrieval; `TripRepository.java` manages persistent storage; `Trip.java` and `Itinerary.java` represent saved trip information. |
| Key methods | Trip saving, loading, and deletion operations; exact method signatures require final source verification. |
| Design patterns | Facade, Repository/DAO |
| Video link/timestamp | [https://youtu.be/_ybrPhysXjA |

**Implementation explanation:** The traveler generates a trip and saves it through the application. The controller passes the request to the repository, which stores the trip information. The traveler can later retrieve the saved trip and display its itinerary. Where supported, stored trips can also be deleted.

**Feature type:** Deterministic.

---






# 3 Implemented Design Patterns

TripGoAI uses object-oriented design patterns to organize its AI agent, trip planning, user interfaces, and itinerary management. Five main patterns are implemented: Facade, Strategy, Adapter, Singleton, and Command.

## 3.1 Facade Pattern

**Design problem addressed:** The GUI and CLI need access to multiple services without directly managing complex planning, AI, validation, and persistence operations.

**Participating classes and roles:**

- `TravelAgentController`  Facade providing a simplified interface to application functionality.
- `TravelPlannerGUI` and `CommandLineInterface`  Clients that interact with the facade.
- `TravelAgent`, `TripPlanner`, `BudgetManager`, `ScheduleManager`, and `TripRepository`  Components responsible for specialized operations.

**Relevant source files and methods:**

- `TravelAgentController.java`  Coordinates planning, research, and trip-management operations.
- `TravelAgent.java`  `planTrip()`, `researchDestination()`, and `interpretTripRequest()`.
- `TravelPlannerGUI.java` and `CommandLineInterface.java`  Invoke controller operations in response to user input.

**Implementation and usefulness:** The GUI and CLI interact with `TravelAgentController` rather than implementing the complete travel-planning workflow themselves. The controller coordinates the relevant services, reducing dependencies between user interfaces and internal components and making the application easier to maintain.

**Stage 1 comparison:** Matches Stage 1. The controller remains the central application facade.

## 3.2 Strategy Pattern

**Design problem addressed:** TripGoAI needs to support different itinerary-planning priorities without changing the main planning workflow.

**Participating classes and roles:**

- `PlanningStrategy`  Strategy interface defining planning behavior.
- `BudgetStrategy` Concrete strategy emphasizing lower costs.
- `BalancedStrategy`  Concrete strategy balancing activities.
- `TripPlanner`  Context that uses the selected strategy.
- `TravelAgent`  Selects the appropriate planning strategy.

**Relevant source files and methods:**

- `PlanningStrategy.java` Defines the common planning interface.
- `BudgetStrategy.java` and `BalancedStrategy.java` — Implement different planning approaches.
- `TripPlanner.java`  `setStrategy()` and `createItinerary()`.
- `TravelAgent.java`  `planTripWithActivities()`.

**Implementation and usefulness:** `TravelAgent` selects either `BudgetStrategy` or `BalancedStrategy` based on the traveler's preferences. The selected strategy is assigned to `TripPlanner`, which creates the itinerary. This allows different planning algorithms to be used through a common interface without duplicating the entire planning process.

**Stage 1 comparison:** Modified. Stage 1 proposed multiple planning strategies, including relaxed-pace planning. Stage 2 focuses on Budget and Balanced strategies supported by the working application.

## 3.3 Adapter Pattern

**Design problem addressed:** The travel agent needs to communicate with an external AI provider without being tightly coupled to provider-specific API details.

**Participating classes and roles:**

- `LLMClient`  Target interface used by the travel agent.
- `OpenAIAdapter`  Adapter connecting the application to the OpenAI API.
- `TravelAgent`  Client requesting AI-generated responses.
- OpenAI API  External AI service.

**Relevant source files and methods:**

- `LLMClient.java`  Defines the AI communication interface.
- `OpenAIAdapter.java` Handles provider-specific API communication.
- `TravelAgent.java`  Uses `llmClient.generate()`.

**Implementation and usefulness:** `TravelAgent` communicates with the AI provider through `LLMClient`, while `OpenAIAdapter` manages the external API integration. This separates AI communication from the application's planning logic and allows the AI provider implementation to be changed without rewriting the agent.

**Stage 1 comparison:** Matches the original design objective. Stage 2 uses `OpenAIAdapter` as the provider-specific integration component.

## 3.4 Singleton Pattern

**Design problem addressed:** Travel preferences and agent memory should be shared consistently instead of being stored in separate memory-manager instances.

**Participating classes and roles:**

- `MemoryManager`  Singleton responsible for storing and retrieving preferences.
- `TravelAgent`  Uses the shared memory component.
- `TravelAgentController`  Coordinates operations that access preferences.

**Relevant source files and methods:**

- `MemoryManager.java`  `getInstance()` and preference-storage operations.
- `TravelAgent.java`  `getMemoryManager()`.

**Implementation and usefulness:** `MemoryManager` provides a shared point of access to traveler preferences. This ensures that remembered information remains consistent across application operations and avoids creating unnecessary duplicate memory managers.

**Stage 1 comparison:** Matches Stage 1. Memory management remains centralized through the Singleton pattern.

## 3.5 Command Pattern

**Design problem addressed:** Natural-language itinerary changes need to be executed safely, validated, and reversed when the traveler requests Undo.

**Participating classes and roles:**

- `ItineraryCommand` — Command interface defining executable and reversible changes.
- `ItineraryCommandFactory` — Creates the appropriate editing command.
- Concrete editing commands — Perform individual itinerary modifications.
- `TravelAgent` — Invoker responsible for executing commands and managing Undo.
- `Itinerary` — Receiver whose activities are modified.

**Relevant source files and methods:**

- `ItineraryCommand.java` — `execute()` and `undo()`.
- `ItineraryCommandFactory.java` — `createCommand()`.
- `TravelAgent.java` — `modifyTrip()` and `undoLastEdit()`.

**Implementation and usefulness:** The traveler provides a natural-language editing instruction. `TravelAgent` interprets the request using AI, identifies the intended activity, and uses `ItineraryCommandFactory` to create an editing command. The command performs the modification, and the updated itinerary is validated. If a problem occurs, the modification can be rolled back. The traveler can also undo the most recent edit.

**Stage 1 comparison:** Matches Stage 1, with additional validation to prevent unintended modifications and support safer rollback.


















# 4 Changes and Rationale

During Stage 2, TripGoAI retained the main objectives and 14 features proposed in Stage 1. Several implementation details were adjusted to improve reliability, simplify integration, and support the working Java application.

The following table summarizes the feature, architecture, class, and design-pattern changes.

| Original design | Implemented change | Reason | Impact |
|---|---|---|---|
| **F02  Natural-language interpretation:** AI interprets travel requests. | Integrated OpenAI API communication through `LLMClient` and `OpenAIAdapter`. | Separate external AI communication from application logic. | Supports natural-language requests while reducing coupling to the AI provider. |
| **F04  Itinerary generation:** Multiple planning strategies, including budget-focused, balanced, and relaxed-pace planning. | Implemented `BudgetStrategy` and `BalancedStrategy` as the main available strategies. | Focus on the planning options supported by the working application. | Preserves the Strategy pattern, but offers fewer planning variations than originally proposed. |
| **F05  Attraction recommendations:** A standalone attraction recommendation feature. | Attraction retrieval and selection are integrated into itinerary generation through `AttractionSearchTool` and `TravelAgent`. | Reuse attraction candidates during trip planning rather than duplicating retrieval workflows. | Attraction recommendations are available within itinerary generation, but are not exposed as a separate GUI operation. |
| **F06–F07  Restaurant and accommodation recommendations:** Dedicated retrieval tools support recommendations. | `RestaurantSearchTool` and `AccommodationSearchTool` are included, while direct end-to-end integration remains unverified. | Specialized retrieval components were developed separately from the main itinerary workflow. | AI-assisted recommendations can be demonstrated, but direct tool execution requires further verification. |
| **F08  Transportation planning:** Support transportation options based on user preferences. | Implemented driving-route estimation using OSRM and OpenStreetMap, with separate origin and destination inputs. | Provide a working external routing integration without relying on unsupported transportation modes. | Users can obtain driving distance and duration, but public transit and other modes are not currently supported. |
| **F09  Budget estimation:** Calculate trip expenses and compare them with the budget. | Added explicit reporting for unknown or unverified activity prices. | Avoid presenting incomplete cost information as accurate. | Improves transparency, although a complete trip budget cannot always be guaranteed. |
| **F11  Weather-aware adjustment:** Automatically revise activities affected by weather. | Implemented forecast retrieval and itinerary weather review through `WeatherTool` and `WeatherAdjustmentService`. | Allow the application to evaluate weather conditions using available forecast information. | Weather review works, but automatic replacement of weather-affected activities has not been fully verified. |
| **F12  Natural-language editing:** Interpret requests and execute reversible itinerary commands. | Added stronger activity identification, replacement validation, and Undo support. | Prevent unintended modifications and preserve itinerary consistency. | Improves the reliability of the Command pattern implementation. |
| **F13  Alternative planning:** `TripPlanner` generates alternative itineraries using planning strategies. | Introduced `AlternativePlanService` to manage targeted and general alternative requests. | Separate alternative-generation logic from standard itinerary creation. | Improves modularity, preserves the original itinerary, and supports more specific replacement requests. |
| **AI architecture:** Stage 1 described `LLMClient` with a provider-specific implementation. | Adopted `OpenAIAdapter` for external API communication. | Isolate provider-specific requests, authentication, and response handling. | Maintains the Adapter pattern and simplifies maintenance. |
| **Tool architecture:** Stage 1 proposed a tool factory and manager for external services. | Specialized tool classes support attraction, destination, restaurant, accommodation, transportation, and weather operations. | Organize external services by responsibility. | Improves separation of concerns; the exact Factory Method pattern implementation requires verification. |
| **Persistence architecture:** Stage 1 proposed `TripRepository` and `DatabaseManager` for trip storage. | Trip saving and loading are supported through the implemented persistence components. | Provide working trip persistence while keeping storage operations separate from user interfaces. | Users can save and retrieve trips; the persistence mechanism must be described according to the final source implementation. |
| **UML design:** Stage 1 diagrams represented the proposed architecture. | Updated the class diagram to include implemented classes such as `OpenAIAdapter` and `AlternativePlanService`, and the active planning strategies. | Ensure the documentation reflects the Stage 2 implementation. | Improves consistency between design documentation and source code. |

## 4.1 Design Pattern Changes

The five principal design patterns described in Section 3 remain part of the implemented architecture:

- **Facade:** Retained through `TravelAgentController`.
- **Strategy:** Retained, with Budget and Balanced strategies as the working options.
- **Adapter:** Implemented using `LLMClient` and `OpenAIAdapter`.
- **Singleton:** Retained through `MemoryManager`.
- **Command:** Retained with additional editing validation and Undo support.

Stage 1 also proposed other architectural patterns, including MVC, Observer, and Factory Method. These are not counted as additional verified patterns in Section 3 unless their complete implementation is confirmed in the submitted source.

## 4.2 New Components and Functionality

The main implementation-level additions and refinements include:

- `AlternativePlanService` for more controlled alternative itinerary generation.
- `WeatherAdjustmentService` for weather-aware itinerary review.
- Driving-route integration using OSRM and OpenStreetMap.
- Additional handling for unknown activity prices.
- Validation of itinerary edits to reduce unintended changes.

These additions support existing Stage 1 requirements rather than introducing new numbered user-facing features.

## 4.3 Overall Impact

The changes made during Stage 2 improved TripGoAI's modularity, reliability, and integration with external services. The main architecture remains consistent with Stage 1, while implementation-specific adjustments address practical limitations.

All 14 original feature categories remain accounted for. The current limitations—including driving-only routing, incomplete price verification, and unverified portions of restaurant, accommodation, and automatic weather-adjustment functionality are documented transparently.


# 6 Development and Verification Summary




Basic functional verification was performed by running TripGoAI through its GUI and CLI and checking the resulting output.

| Component or feature | Verification performed | Result |
|---|---|---|
| GUI and CLI | Launched both interfaces and executed application operations. | Working |
| Preference memory | Saved and retrieved travel preferences. | Passed |
| AI integration | Submitted natural-language requests and examined AI-generated responses. | Passed |
| Itinerary generation | Generated multi-day trips with scheduled activities. | Passed |
| Attraction recommendations | Generated itineraries using destination and interest preferences. | Passed within planning workflow |
| Budget management | Calculated known activity costs and remaining budgets. | Passed |
| Schedule validation | Checked generated itineraries for overlapping activities. | Passed for non-conflicting schedules |
| Transportation routing | Tested routes between locations in Paris. | Passed for driving routes |
| Weather integration | Retrieved forecasts and reviewed itineraries. | Forecast and review demonstrated |
| Itinerary editing | Modified activities using natural-language instructions and tested Undo. | Passed in observed scenarios |
| Alternative planning | Generated targeted and general alternative plans. | Passed |
| Packing list | Generated packing recommendations from travel information. | Passed |
| Trip persistence | Tested saving and loading trips. | Passed in observed scenarios |

For example, one Rome itinerary contained nine activities across November 16–18, 2026. The application reported a $105 total of known activity costs against a $2,000 budget, a remaining balance of $1,895, and no overlapping activities.

Restaurant and accommodation recommendation components are present, but their complete retrieval-tool integration has not been independently verified.

## 6.3 Important Defects Found and Corrected

| Defect identified | Correction implemented | Result |
|---|---|---|
| Transportation routing did not correctly distinguish between the starting location and destination. | Updated the GUI to request separate origin and destination inputs. | Corrected route calculation between distinct locations. |
| Transportation interface offered options not fully supported by the routing implementation. | Restricted the available transportation mode to driving. | Interface now reflects supported functionality. |
| Alternative planning could produce unwanted changes when a requested attraction was not correctly identified. | Improved target identification and validation in `AlternativePlanService`. | Reduced unintended replacements. |
| General alternative requests could produce unnecessary replacements. | Improved candidate selection and preservation of existing activities. | Better preservation of the original itinerary. |
| Itinerary modification required reliable reversal. | Used command-based editing and Undo to restore previous itinerary state. | Editing and Undo demonstrated successfully. |
| Some activity prices were unavailable or uncertain. | Added explicit unknown-price reporting and price-audit information. | Budget reports no longer treat every price as verified. |

These corrections were checked through additional application runs and observed outputs.

## 6.4 Remaining Limitations

Although the main application functionality is operational, several limitations remain:

- Transportation planning currently supports driving routes only.
- Activity prices, opening hours, travel times, and visit durations may be incomplete or unverified.
- Weather-aware review works, but automatic replacement of affected activities has not been fully verified.
- Restaurant and accommodation retrieval tools require further end-to-end verification.
- AI-generated recommendations depend on external services and may require independent accuracy checks.
- AI-dependent functionality requires internet access and a valid API key.

## 6.5 Stage 2 Verification Scope

Stage 2 verification focused on ensuring that the application runs, its main features produce meaningful results, and important defects are corrected.




# 7 Demonstration Index



| Feature ID | Feature Name | Demonstration | Video Link |
|---|---|---|---|
| F01 | Travel Profile and Preferences | Save and retrieve travel preferences using the CLI. | [https://www.youtube.com/watch?v=IbsQ1SF9MKA] |
| F02 | Natural-Language Travel Request Interpretation | Enter a natural-language travel request and display the extracted trip details. | [https://www.youtube.com/watch?v=zUhHfCq8wIc] |
| F03 | Destination Research | Research a destination and display the retrieved information. | [https://www.youtube.com/watch?v=j2muNB-y1k0] |
| F04 | Multi-Day Itinerary Generation | Generate a multi-day itinerary showing activities, dates, and times. | [https://www.youtube.com/watch?v=7xS2FCgBvjY] |
| F05 | Attraction Recommendations | Demonstrate attraction retrieval and selection during itinerary generation. | [https://www.youtube.com/watch?v=7xS2FCgBvjY] |
| F06 | Restaurant Recommendations | Request restaurant recommendations through the CLI. | [https://www.youtube.com/watch?v=3TOSGm8SLlw] |
| F07 | Accommodation Planning | Request accommodation recommendations through the CLI. | [https://www.youtube.com/watch?v=Q6JNlR8QWXs] |
| F08 | Transportation Planning | Calculate a driving route with distance and estimated duration. | [https://www.youtube.com/watch?v=Axwdm_TbkFM] |
| F09 | Budget Estimation and Tracking | Display known activity costs, remaining budget, and price warnings. | [https://www.youtube.com/watch?v=0KZXTJO72wo] |
| F10 | Schedule Conflict Detection | Display the itinerary schedule-validation results. | [https://www.youtube.com/watch?v=0KZXTJO72wo] |
| F11 | Weather-Aware Itinerary Adjustment | Display a weather forecast and review an itinerary for weather-related issues. | [https://www.youtube.com/watch?v=YQro9cwi-44] |
| F12 | Natural-Language Itinerary Editing | Modify an activity using natural language and demonstrate Undo. | [https://youtu.be/y5PGhqNlcTM] |
| F13 | Alternative Plan Generation | Generate an alternative itinerary while preserving the original plan. | [https://www.youtube.com/watch?v=BSxuUIRypv0] |
| F14 | Packing List Generation | Generate packing recommendations based on travel details. | [https://youtu.be/YgNR-uhHJWw] |





