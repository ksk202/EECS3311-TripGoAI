

## F01  User Travel Profile and Preferences

**Related Use Case:** UC-02  Set Preferences  
**Related Sequence Diagram:** SD03  Set Preferences

**Classes involved:**
- `TravelPlannerGUI`  allows the traveler to enter preferences.
- `TravelAgentController`  coordinates the preference request.
- `TravelAgent`  handles the preference operation.
- `MemoryManager`  stores and retrieves traveler preferences.
- `TravelProfile`  represents the traveler's stored profile.

**Important methods:**
- `MemoryManager.savePreference(key, value)`
- `MemoryManager.getPreferences()`

**Execution:**  
When the traveler enters or changes a preference, the request is passed through the application interface and controller. `MemoryManager.savePreference()` stores the preference so that `TravelAgent` can use it during later planning and recommendation operations. Stored preferences can be retrieved using `getPreferences()`.


## F02  Natural-Language Trip Planning

**Related Use Case:** UC-01  Plan Trip  
**Related Sequence Diagram:** SD01  Plan Trip

**Classes involved:**
- `TravelPlannerGUI`  accepts the natural-language request and displays the result.
- `CommandLineInterface`  provides CLI access to the planning functionality.
- `TravelAgentController`  coordinates the planning request.
- `TravelAgent`  interprets the request and manages planning.
- `TripRequest`  represents the extracted trip requirements.
- `LLMClient`  provides communication with the AI model.

**Important methods:**
- `TravelPlannerGUI.submitRequest()`
- `TravelAgentController.planTrip(request)`
- `TravelAgent.interpretRequest(text)`
- `TravelAgent.planTrip(request)`
- `LLMClient.extractTripRequest(text)`

**Execution:**  
The traveler submits a natural-language request through the GUI or CLI. `TravelAgentController.planTrip()` passes the request into the agent workflow. `TravelAgent` interprets the request and uses `LLMClient` to extract structured trip requirements. These requirements are represented by `TripRequest` and used to generate the trip. If the AI service fails, the error flow in SD01 returns an error to the GUI.


## F03  Destination Research

**Related Use Case:** UC-03 — Research Destination  
**Related Sequence Diagram:** SD02 — Search Travel Information

**Classes involved:**
- `TravelAgentController` — receives the information request.
- `TravelAgent` — coordinates the request.
- `ToolManager` — selects and executes the appropriate tool.
- `TravelTool` — defines the common tool interface.
- `DestinationSearchTool` — retrieves destination information.

**Important methods:**
- `TravelAgentController.getRecommendations(type)`
- `ToolManager.executeTool(type, input)`
- `TravelTool.execute(input)`

**Execution:**  
When destination information is requested, the request enters the agent architecture through the controller. `ToolManager.executeTool()` selects the appropriate travel tool, such as `DestinationSearchTool`. The tool retrieves information from an external travel-data service and returns the results through the agent architecture.


## F04  Multi-Day Itinerary Generation

**Related Use Case:** UC-01  Plan Trip  
**Related Sequence Diagram:** SD01  Plan Trip

**Classes involved:**
- `TravelAgent` — coordinates trip generation.
- `TripPlanner` — constructs the itinerary.
- `PlanningStrategy` — defines the planning approach.
- `Itinerary` — represents the generated schedule.
- `LLMClient` — supports AI-assisted generation.

**Important methods:**
- `TravelAgent.planTrip(request)`
- `TripPlanner.createItinerary(request)`
- `TripPlanner.setStrategy(strategy)`
- `PlanningStrategy.createPlan(request)`
- `LLMClient.generate(prompt)`

**Execution:**  
After the travel requirements have been interpreted, `TravelAgent` invokes the planning process. `TripPlanner.createItinerary()` uses the selected `PlanningStrategy` to construct a multi-day itinerary. AI-generated information can be obtained through `LLMClient`, and the resulting itinerary is returned to the traveler through the controller and GUI.


## F05  Attraction Recommendations

**Related Use Case:** UC-04  Get Travel Recommendations  
**Related Sequence Diagram:** SD02  Search Travel Information

**Classes involved:**
- `TravelAgentController` — coordinates the recommendation request.
- `TravelAgent` — manages the recommendation process.
- `ToolManager` — selects the required tool.
- `AttractionSearchTool` — retrieves attraction information.

**Important methods:**
- `TravelAgentController.getRecommendations(type)`
- `ToolManager.executeTool(type, input)`
- `TravelTool.execute(input)`

**Execution:**  
When the traveler requests attraction recommendations, the controller initiates the recommendation process. `ToolManager` selects `AttractionSearchTool`, which retrieves relevant attraction information. The results are returned through the agent architecture and presented according to the traveler's trip context and preferences.


## F06  Restaurant Recommendations

**Related Use Case:** UC-04  Get Travel Recommendations  
**Related Sequence Diagram:** SD02  Search Travel Information

**Classes involved:**
- `TravelAgentController`  coordinates the restaurant request.
- `TravelAgent`  manages the recommendation process.
- `ToolManager`  selects the appropriate tool.
- `RestaurantSearchTool`  retrieves restaurant information.

**Important methods:**
- `TravelAgentController.getRecommendations(type)`
- `ToolManager.executeTool(type, input)`
- `TravelTool.execute(input)`

**Execution:**  
The traveler provides restaurant requirements such as location, cuisine, dietary preferences, or budget. `ToolManager.executeTool()` invokes `RestaurantSearchTool`, which retrieves restaurant information from the external service. The information is returned through the agent and presented as relevant recommendations.


## F07  Accommodation Planning

**Related Use Case:** UC-04  Get Travel Recommendations  
**Related Sequence Diagram:** SD02  Search Travel Information

**Classes involved:**
- `TravelAgentController`  receives the accommodation request.
- `TravelAgent`  coordinates recommendation logic.
- `ToolManager`  manages access to travel tools.
- `AccommodationSearchTool`  retrieves accommodation information.

**Important methods:**
- `TravelAgentController.getRecommendations(type)`
- `ToolManager.executeTool(type, input)`
- `TravelTool.execute(input)`

**Execution:**  
The traveler specifies accommodation requirements such as destination, dates, preferred area, and budget. `ToolManager` invokes `AccommodationSearchTool` to retrieve suitable information. The results are returned through the agent architecture and presented according to the traveler's requirements.


## F08  Transportation Planning

**Related Use Case:** UC-05  Plan Transportation  
**Related Sequence Diagram:** SD02  Search Travel Information

**Classes involved:**
- `TravelAgent`  coordinates transportation requests.
- `ToolManager`  selects the required tool.
- `TravelTool`  defines the common tool operation.
- `TransportationTool`  retrieves transportation information.
- External map/travel services — provide route and transportation data.

**Important methods:**
- `ToolManager.executeTool(type, input)`
- `TravelTool.execute(input)`
- `TransportationTool.execute(input)`

**Execution:**  
When transportation information is required, `TravelAgent` provides the relevant origin, destination, and trip information to `ToolManager`. The manager invokes `TransportationTool`, which obtains route or transportation information from the applicable external service. The returned information can then be evaluated against the traveler's trip requirements.


## F09 — Budget Estimation and Tracking

**Related Use Case:** UC-06  View Budget  
**Related Sequence Diagram:** SD01  Plan Trip (planning/validation flow)

**Classes involved:**
- `TravelAgentController`  provides access to budget information.
- `TripPlanner`  constructs the itinerary being evaluated.
- `BudgetManager`  calculates and validates estimated costs.
- `Itinerary`  contains the activities used in the calculation.

**Important methods:**
- `TravelAgentController.showBudget()`
- `BudgetManager.calculateTotal(itinerary)`
- `BudgetManager.isWithinBudget(limit)`

**Execution:**  
During itinerary planning, `BudgetManager.calculateTotal()` calculates the estimated cost of the itinerary. `isWithinBudget()` determines whether the plan satisfies the traveler's budget limit. `TravelAgentController.showBudget()` provides the budget information to the user.


## F10  Schedule Conflict Detection

**Related Use Case:** UC-07 — Check Schedule Conflicts  
**Related Sequence Diagram:** SD01 — Plan Trip (planning/validation flow)

**Classes involved:**
- `TravelAgent`  manages the planning process.
- `TripPlanner`  creates the proposed itinerary.
- `ScheduleManager`  checks the itinerary for conflicts.
- `Itinerary`  contains the activities being validated.

**Important methods:**
- `ScheduleManager.checkConflicts(itinerary)`
- `TripPlanner.createItinerary(request)`

**Execution:**  
When an itinerary is generated, `ScheduleManager.checkConflicts()` examines its activities and timing to identify scheduling conflicts. This validation helps ensure that the proposed itinerary is feasible before it is presented to the traveler.


## F11  Weather-Aware Itinerary Adjustment

**Related Use Case:** UC-08 — Adjust Itinerary for Weather  
**Related Sequence Diagram:** SD04 — Modify Itinerary & Weather Adjustment

**Classes involved:**
- `TravelAgentController` — coordinates weather and modification operations.
- `TravelAgent` — evaluates the requested change.
- `ToolManager` — manages the weather tool.
- `WeatherTool` — retrieves weather information.
- `Itinerary` — represents the itinerary being adjusted.

**Important methods:**
- `TravelAgentController.checkWeather()`
- `TravelAgent.modifyTrip(instruction)`
- `ToolManager.executeTool(type, input)`
- `WeatherTool.execute(input)`
- `Itinerary.addActivity(activity)`
- `Itinerary.removeActivity(activity)`

**Execution:**  
When weather-aware adjustment is required, `TravelAgent` uses `ToolManager` to invoke `WeatherTool`. The returned weather information is evaluated against the current itinerary. If an activity is unsuitable, the agent can modify the itinerary using operations such as `removeActivity()` and `addActivity()`. SD04 also represents the alternative flow when the requested change cannot be applied.


## F12  Natural-Language Itinerary Editing

**Related Use Case:** UC-09  Modify Itinerary  
**Related Sequence Diagram:** SD04  Modify Itinerary & Weather Adjustment

**Classes involved:**
- `TravelPlannerGUI` — receives the modification instruction.
- `TravelAgentController` — coordinates the request.
- `TravelAgent` — interprets and performs the modification.
- `LLMClient` — interprets natural-language modification instructions.
- `Itinerary` — represents the itinerary being changed.

**Important methods:**
- `TravelAgentController.modifyTrip(instruction)`
- `TravelAgent.modifyTrip(instruction)`
- `LLMClient.generate(prompt)`
- `Itinerary.addActivity(activity)`
- `Itinerary.removeActivity(activity)`

**Execution:**  
The traveler enters a natural-language modification request. `TravelAgentController.modifyTrip()` forwards it to `TravelAgent`. The agent uses the AI component to interpret the requested change and updates the `Itinerary` using operations such as `addActivity()` or `removeActivity()`. The updated trip is returned to the GUI.


## F13  Alternative Plan Generation

**Related Use Case:** UC-10  Generate Alternative Plan  
**Related Sequence Diagram:** SD05  Generate Alternative Plan

**Classes involved:**
- `TravelPlannerGUI`  receives the alternative-plan request.
- `TravelAgentController`  coordinates the request.
- `TravelAgent`  generates an alternative trip.
- `LLMClient`  supports AI generation.
- `TripPlanner`  constructs the alternative itinerary.

**Important methods:**
- `TravelAgent.generateAlternative(trip)`
- `LLMClient.generate(prompt)`
- `TripPlanner.createItinerary(request)`

**Execution:**  
When the traveler requests an alternative plan, `TravelAgent.generateAlternative()` uses the existing trip as context. `LLMClient.generate()` assists in producing an alternative, and `TripPlanner.createItinerary()` constructs the resulting itinerary. If a suitable alternative is generated, it is returned through the controller and displayed to the traveler. If generation fails, the error flow returns an appropriate message.


## F14  Packing List Generation

**Related Use Case:** UC-11 — Generate Packing List  
**Related Sequence Diagram:** Covered by the AI/agent generation workflow

**Classes involved:**
- `TravelAgent`  coordinates packing-list generation.
- `LLMClient`  generates personalized packing recommendations.
- `MemoryManager`  provides stored traveler preferences.
- `TravelProfile`  represents relevant traveler preferences.

**Important methods:**
- `TravelAgent.generatePackingList(trip)`
- `LLMClient.generate(prompt)`
- `MemoryManager.getPreferences()`

**Execution:**  
When the traveler requests a packing list, `TravelAgent.generatePackingList()` uses the current trip information and stored preferences from `MemoryManager`. Relevant context is supplied to `LLMClient.generate()`, which generates a personalized packing list based on the trip. The generated list is then returned to the traveler.
