

## F01 ( User Travel Profile and Preferences )

**Related Use Case:** UC-02  "Set Preferences"  
**Related Sequence Diagram:** SD03  "Modify Trip & Preferences"

**Classes involved:**
- `TravelProfile` : represents the traveler's stored profile, including budget level and preferences.
- `TravelAgent` : uses stored preferences when generating plans and recommendations.
- `MemoryManager` : stores and retrieves user preferences.
- `TravelAgentController` : coordinates requests between the interface and application components.

**Important methods:**
- `MemoryManager.savePreference(key, value)`
- `MemoryManager.getPreferences()`

**Execution:**  
When the traveler enters or changes a preference, the request is passed through the application interface. The preference is stored using `MemoryManager.savePreference()`. `MemoryManager` maintains these preferences so that `TravelAgent` can use them during later planning and recommendation operations. Stored preferences can be retrieved using `getPreferences()`.



## F02 (Natural-Language Trip Planning)

**Related Use Case:** UC-01  "Plan Trip"  
**Related Sequence Diagram:** SD01  "Plan Trip & AI Generation"

**Classes involved:**
- `TravelPlannerGUI` : accepts the traveler's natural-language request and displays the resulting trip.
- `CommandLineInterface` : provides CLI access to the same planning functionality.
- `TravelAgentController` : coordinates the planning request.
- `TravelAgent` : interprets the natural-language request and manages the planning process.
- `TripRequest` : represents the extracted destination, dates, budget, and other requirements.
- `LLMClient` : provides an interface for communication with the AI model.

**Important methods:**
- `TravelPlannerGUI.submitRequest()`
- `TravelAgentController.planTrip(request)`
- `TravelAgent.interpretRequest(text)`
- `TravelAgent.planTrip(request)`
- `LLMClient.extractTripRequest(text)`

**Execution:**  
The traveler submits a natural-language request through the GUI or CLI. `TravelAgentController.planTrip()` passes the request to `TravelAgent`, which interprets the text and converts the relevant information into a `TripRequest`. The validated request is then used by the agent's planning components to produce a personalized trip. If the AI service fails during generation, SD01 also demonstrates an error flow in which the GUI displays an error to the traveler.



## F03 ( Destination Research)

**Related Use Case:** UC-09  "Search Travel Options"  
**Related Sequence Diagram:** SD02  "Search Travel Information"

**Classes involved:**
- `TravelAgentController` : receives the request for travel information.
- `TravelAgent` : determines that external travel information is required.
- `ToolManager` : manages and executes the appropriate travel tool.
- `TravelTool` : provides the common interface for travel-information tools.
- `LLMClient` : supports AI processing and organization of retrieved information.

**Important methods:**
- `TravelAgentController.getRecommendations(type)`
- `ToolManager.executeTool(type, input)`
- `TravelTool.execute(input)`
- `LLMClient.generate(prompt)`

**Execution:**  
When destination information is requested, the controller passes the request into the agent architecture. `ToolManager.executeTool()` selects the appropriate `TravelTool`, which obtains information from an external travel-data service. The retrieved information can then be processed by the AI components and returned as useful destination information instead of relying on unsupported information.



## F04 (Multi-Day Itinerary Generation)

**Related Use Cases:** UC-01  "Plan Trip"; UC-07  "Generate AI Itinerary"  
**Related Sequence Diagram:** SD01  "Plan Trip & AI Generation"

**Classes involved:**
- `TravelAgent` : coordinates AI-based trip generation.
- `TripPlanner` : constructs the itinerary.
- `PlanningStrategy` : defines the strategy used to create a plan.
- `Itinerary` : represents the collection of planned activities.
- `BudgetManager` : calculates and validates estimated costs.
- `ScheduleManager` : checks the itinerary for conflicts.
- `LLMClient` : communicates with the AI model.

**Important methods:**
- `TravelAgent.planTrip(request)`
- `TripPlanner.createItinerary(request)`
- `TripPlanner.setStrategy(strategy)`
- `LLMClient.generate(prompt)`
- `BudgetManager.calculateTotal(itinerary)`
- `ScheduleManager.checkConflicts(itinerary)`

**Execution:**  
After the travel requirements have been interpreted, `TravelAgent.planTrip()` invokes `TripPlanner.createItinerary()`. The planner uses a `PlanningStrategy` and AI generation through `LLMClient` to construct the itinerary. `BudgetManager` supports cost validation and `ScheduleManager` supports conflict checking. The resulting trip is returned through the controller and displayed to the traveler.



## F05 (Attraction Recommendations)

**Related Use Case:** UC-09  "Search Travel Options"  
**Related Sequence Diagram:** SD02  "Search Travel Information"

**Classes involved:**
- `TravelAgentController` : coordinates the recommendation request.
- `TravelAgent` : handles the recommendation process.
- `ToolManager` : selects and executes a travel-information tool.
- `TravelTool` : retrieves external travel information.

**Important methods:**
- `TravelAgentController.getRecommendations(type)`
- `ToolManager.executeTool(type, input)`
- `TravelTool.execute(input)`

**Execution:**  
When the traveler requests attraction recommendations, `getRecommendations()` initiates the recommendation process. The agent uses `ToolManager` to execute the appropriate `TravelTool`. Attraction information retrieved from the external travel service is returned to the agent, which can use the traveler's requirements and trip context to produce relevant recommendations.



## F06 ( Restaurant Recommendations)

**Related Use Case:** UC-09  "Search Travel Options"  
**Related Sequence Diagram:** SD02  "Search Travel Information"

**Classes involved:**
- `TravelAgentController` : coordinates the restaurant recommendation request.
- `TravelAgent` : manages the recommendation process.
- `ToolManager` : invokes the required travel-information tool.
- `TravelTool` : retrieves restaurant-related information.

**Important methods:**
- `TravelAgentController.getRecommendations(type)`
- `ToolManager.executeTool(type, input)`
- `TravelTool.execute(input)`

**Execution:**  
The traveler provides restaurant requirements such as location, cuisine, dietary restrictions, or budget. The request is coordinated through `TravelAgentController`, and `ToolManager.executeTool()` invokes the relevant implementation of `TravelTool`. Retrieved restaurant information can then be evaluated against the user's preferences before recommendations are presented.



## F07 ( Accommodation Planning)

**Related Use Case:** UC-09  "Search Travel Options"  
**Related Sequence Diagram:** SD02  "Search Travel Information"

**Classes involved:**
- `TravelAgentController` : receives the accommodation request.
- `TravelAgent` : coordinates the recommendation logic.
- `ToolManager` : manages access to external travel tools.
- `TravelTool` : retrieves accommodation information.

**Important methods:**
- `TravelAgentController.getRecommendations(type)`
- `ToolManager.executeTool(type, input)`
- `TravelTool.execute(input)`

**Execution:**  
The traveler specifies accommodation requirements such as destination, dates, preferred area, and budget. The agent uses `ToolManager` to invoke the appropriate `TravelTool`. The retrieved accommodation information is returned through the agent architecture so suitable options can be presented according to the user's requirements.



## F08 (Transportation Planning)

**Related Use Cases:** UC-08  "Get Route/Map Data"; UC-09  "Search Travel Options"  
**Related Sequence Diagram:** SD02  "Search Travel Information"

**Classes involved:**
- `TravelAgent` : coordinates transportation-information requests.
- `ToolManager` : selects the required travel tool.
- `TravelTool` : retrieves transportation or route-related information.
- External travel/map services — provide current transportation or route information.

**Important methods:**
- `ToolManager.executeTool(type, input)`
- `TravelTool.execute(input)`

**Execution:**  
When transportation information is required, the agent supplies the origin, destination, and relevant trip information to `ToolManager`. The manager invokes a suitable `TravelTool`, which communicates with the applicable external service. The returned information can then be evaluated against factors such as cost, timing, and travel preferences.



## F09 (Budget Estimation and Tracking)

**Related Use Cases:** UC-01  "Plan Trip"; UC-04  "Modify Itinerary"  
**Related Sequence Diagrams:** SD01  "Plan Trip & AI Generation"; SD03  "Modify Trip & Preferences"

**Classes involved:**
- `TravelAgentController` : provides access to budget information.
- `TripPlanner` : creates the itinerary whose costs must be evaluated.
- `BudgetManager` : calculates total estimated costs and checks the budget limit.
- `Itinerary` : contains the activities used in the calculation.

**Important methods:**
- `TravelAgentController.showBudget()`
- `BudgetManager.calculateTotal(itinerary)`
- `BudgetManager.isWithinBudget(limit)`

**Execution:**  
After an itinerary is created or changed, `BudgetManager.calculateTotal()` calculates its estimated total cost. `isWithinBudget()` determines whether the plan satisfies the traveler's budget limit. `TravelAgentController.showBudget()` provides access to budget information. This deterministic validation supports both initial trip generation and subsequent itinerary modification.



## F10 (Schedule Conflict Detection)

**Related Use Cases:** UC-01  "Plan Trip"; UC-04  "Modify Itinerary"  
**Related Sequence Diagrams:** SD01  "Plan Trip & AI Generation"; SD03  "Modify Trip & Preferences"

**Classes involved:**
- `TravelAgent` : manages planning and modification requests.
- `TripPlanner` : creates the proposed itinerary.
- `ScheduleManager` : checks the itinerary for scheduling conflicts.
- `Itinerary` : contains the activities and schedule being validated.

**Important methods:**
- `ScheduleManager.checkConflicts(itinerary)`
- `TripPlanner.createItinerary(request)`
- `TravelAgent.modifyTrip(instruction)`

**Execution:**  
When an itinerary is generated or modified, its activities must form a valid schedule. `ScheduleManager.checkConflicts()` examines the itinerary to identify scheduling conflicts. If modification is requested, `TravelAgent.modifyTrip()` handles the requested change, after which the resulting itinerary can be checked again before being presented to the traveler.



## F11 (Weather-Aware Itinerary Adjustment)

**Related Use Cases:** UC-09  "Search Travel Options"; UC-04  "Modify Itinerary"  
**Related Sequence Diagrams:** SD02  "Search Travel Information"; SD03  "Modify Trip & Preferences"

**Classes involved:**
- `TravelAgentController` — provides the weather-check operation.
- `TravelAgent` : reasons about the retrieved information and trip changes.
- `ToolManager` : manages the external information tool.
- `TravelTool` : retrieves weather-related information through the generalized tool interface.
- `Itinerary` : represents the plan that may need modification.

**Important methods:**
- `TravelAgentController.checkWeather()`
- `ToolManager.executeTool(type, input)`
- `TravelTool.execute(input)`
- `TravelAgent.modifyTrip(instruction)`

**Execution:**  
When weather information is requested, the system uses the tool architecture represented in SD02 to obtain external information. The agent evaluates the information against the current itinerary. If an activity should be moved or replaced, the modification architecture represented in SD03 is used to update the itinerary. If weather information cannot be obtained, the original itinerary can remain unchanged rather than relying on invented weather data.



## F12 (Natural-Language Itinerary Editing)

**Related Use Case:** UC-04  "Modify Itinerary"  
**Related Sequence Diagram:** SD03  "Modify Trip & Preferences"

**Classes involved:**
- `TravelPlannerGUI` : receives the traveler's modification instruction and displays the updated trip.
- `TravelAgentController` : coordinates the modification request.
- `TravelAgent` : interprets and performs the requested modification.
- `Itinerary` : represents the itinerary being changed.

**Important methods:**
- `TravelAgentController.modifyTrip(instruction)`
- `TravelAgent.modifyTrip(instruction)`
- `Itinerary.addActivity(activity)`
- `Itinerary.removeActivity(activity)`

**Execution:**  
The traveler enters a natural-language modification instruction through the interface. `TravelAgentController.modifyTrip()` forwards the instruction to `TravelAgent.modifyTrip()`. The agent interprets the requested change and updates the `Itinerary`, using operations such as `addActivity()` or `removeActivity()` when appropriate. The updated trip is then returned to the GUI for display.



## F13 (Alternative Plan Generation)

**Related Use Cases:** UC-01  "Plan Trip"; UC-07  "Generate AI Itinerary"  
**Related Sequence Diagram:** SD01  "Plan Trip & AI Generation"

**Classes involved:**
- `TravelAgent` : coordinates generation of another plan.
- `TripPlanner` : creates the alternative itinerary.
- `PlanningStrategy` : allows the planning approach to vary.
- `BudgetManager` : validates the alternative plan's cost.
- `ScheduleManager` : validates its schedule.
- `LLMClient` : supports AI generation.

**Important methods:**
- `TravelAgent.planTrip(request)`
- `TripPlanner.createItinerary(request)`
- `TripPlanner.setStrategy(strategy)`
- `LLMClient.generate(prompt)`
- `BudgetManager.calculateTotal(itinerary)`
- `ScheduleManager.checkConflicts(itinerary)`

**Execution:**  
When the traveler requests an alternative plan, the existing trip requirements and any new constraints are processed by `TravelAgent`. `TripPlanner` can apply an appropriate `PlanningStrategy` and generate another itinerary with support from `LLMClient`. `BudgetManager` and `ScheduleManager` provide deterministic validation of the alternative before it is returned to the traveler.



## F14 (Packing List Generation)

**Related Use Case:** UC-09  "Search Travel Options"  
**Related Sequence Diagram:** SD02  "Search Travel Information"

**Classes involved:**
- `TravelAgent` : coordinates the generation of personalized recommendations.
- `ToolManager` : obtains relevant external trip information when required.
- `LLMClient` : generates the personalized packing recommendations.
- `MemoryManager` : provides stored travel preferences and context.

**Important methods:**
- `ToolManager.executeTool(type, input)`
- `LLMClient.generate(prompt)`
- `MemoryManager.getPreferences()`

**Execution:**  
When the traveler requests a packing list, `TravelAgent` uses available trip context and stored preferences from `MemoryManager`. Relevant external information, such as weather-related travel data, can be obtained through `ToolManager`. This context is supplied to the AI through `LLMClient.generate()`, which produces personalized packing recommendations. If external weather information is unavailable, the agent can still generate a general list using the available trip information.
