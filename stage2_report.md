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

TripGoAI uses object-oriented design patterns to organize its AI agent, trip planning, user interfaces, and itinerary management. Five main patterns are described below: Facade, Strategy, Adapter, Singleton, and Command.

## 1 Facade Pattern

**Design problem addressed:** The GUI and CLI need access to multiple services without directly managing complex planning, AI, validation, and persistence operations.

**Participating classes and roles:**
- `TravelAgentController`  Facade that provides a simplified interface to application functionality.
- `TravelPlannerGUI` and `CommandLineInterface`  Clients that interact with the facade.
- `TravelAgent`, `TripPlanner`, `BudgetManager`, `ScheduleManager`, and `TripRepository`  Components responsible for specialized operations.

**Relevant source files and methods:**
- `TravelAgentController.java`  Application-level operations for planning, research, and trip management.
- `TravelAgent.java`  `planTrip()`, `researchDestination()`, and `interpretTripRequest()`.
- `TravelPlannerGUI.java` and `CommandLineInterface.java`  User-interface actions that invoke the controller.

**Implementation and usefulness:** The GUI and CLI communicate through `TravelAgentController` rather than implementing the complete travel-planning workflow themselves. The controller coordinates the relevant services, reducing dependencies between the interfaces and internal implementation.

**Stage 1 comparison:** Matches Stage 1. The controller remains the central application facade.

## 2 Strategy Pattern

**Design problem addressed:** TripGoAI needs to support different itinerary planning priorities without changing the main planning workflow.

**Participating classes and roles:**
- `PlanningStrategy`  Strategy interface defining the planning behavior.
- `BudgetStrategy`  Concrete strategy emphasizing lower costs.
- `BalancedStrategy`  Concrete strategy balancing activities.
- `TripPlanner`  Context that uses the selected strategy.
- `TravelAgent`  Selects the appropriate strategy.

**Relevant source files and methods:**
- `PlanningStrategy.java`
- `BudgetStrategy.java`
- `BalancedStrategy.java`
- `TripPlanner.java`  `setStrategy()` and `createItinerary()`.
- `TravelAgent.java`  `planTripWithActivities()`.

**Implementation and usefulness:** `TravelAgent` selects either `BudgetStrategy` or `BalancedStrategy` based on the planning preference. The selected strategy is assigned to `TripPlanner`, which creates the itinerary. This allows different planning algorithms to be used through a common interface without duplicating the entire planning process.

**Stage 1 comparison:** Modified. Stage 1 proposed multiple planning strategies, including relaxed-pace planning. The verified Stage 2 workflow uses Budget and Balanced strategies. This focuses the implementation on the planning approaches supported by the working application.

## 3 Adapter Pattern

**Design problem addressed:** The travel agent should communicate with an external AI provider without being tightly coupled to provider-specific API details.

**Participating classes and roles:**
- `LLMClient`  Target interface used by the travel agent.
- `OpenAIAdapter`  Adapter that integrates the external OpenAI API.
- `TravelAgent`  Client that requests AI-generated responses.
- OpenAI API  External service.

**Relevant source files and methods:**
- `LLMClient.java`
- `OpenAIAdapter.java`  Provider-specific API integration.
- `TravelAgent.java`  Calls `llmClient.generate()`.

**Implementation and usefulness:** `TravelAgent` communicates with the AI integration through `LLMClient`. The adapter handles communication with the external provider. This separation allows the application to change its AI implementation without rewriting the agent's planning and interpretation logic.

**Stage 1 comparison:** Matches the original design objective. Stage 2 uses `OpenAIAdapter` as the provider-specific integration component.

## 4 Singleton Pattern

**Design problem addressed:** Travel preferences and agent memory should be shared consistently instead of being stored in unrelated memory-manager instances.

**Participating classes and roles:**
- `MemoryManager`  Singleton responsible for storing and retrieving preferences
- `TravelAgent`  Uses the shared memory component
- `TravelAgentController`  Coordinates operations that access preferences

**Relevant source files and methods:**
- `MemoryManager.java`  `getInstance()` and preference-storage operations
- `TravelAgent.java`  `getMemoryManager()`

**Implementation and usefulness:** The Singleton pattern provides a shared point of access to traveler preferences. This helps keep remembered preferences consistent across application operations and avoids unnecessary duplicate memory managers.

**Stage 1 comparison:** Matches Stage 1. Memory management remains a centralized component.

## 5 Command Pattern

**Design problem addressed:** Natural-language itinerary changes need to be executed safely, validated, and reversible through Undo.

**Participating classes and roles:**
- `ItineraryCommand`  Command interface defining executable and reversible changes
- `ItineraryCommandFactory`  Creates the appropriate editing command
- Concrete editing commands  Perform individual itinerary modifications
- `TravelAgent`  Invoker that executes commands and manages Undo
- `Itinerary`  Receiver whose activities are modified

**Relevant source files and methods:**
- `ItineraryCommand.java``execute()` and `undo()`
- `ItineraryCommandFactory.java` `createCommand()`
- `TravelAgent.java` `modifyTrip()` and `undoLastEdit()`

**Implementation and usefulness:** The traveler provides an editing instruction. `TravelAgent` uses AI to interpret the request, verifies the intended activity, and asks `ItineraryCommandFactory` to create the command. The command executes the change, after which the itinerary is validated. If a problem occurs, the system can roll back the modification. The most recent command can also be undone by the traveler.

**Stage 1 comparison:** Matches Stage 1, with additional validation to prevent unintended edits and support safer rollback.

## 6 Updated UML Diagrams

The Stage 2 UML class diagram reflects the implemented architecture, including the GUI and CLI, application controller, AI agent, planning strategies, external tools, memory management, itinerary editing, persistence, and domain classes.

Compared with Stage 1, the diagram includes implementation-level components such as `OpenAIAdapter` and `AlternativePlanService`. It also reflects the Budget and Balanced planning strategies used in the current workflow.

The affected sequence diagrams should be updated where the actual implementation differs significantly from Stage 1, particularly for transportation routing, alternative plan generation, and AI-supported itinerary editing.

**Updated UML class diagram:** Insert the exported Stage 2 class diagram here.

**Updated sequence diagrams:** Include revised diagrams for modified workflows where necessary.

