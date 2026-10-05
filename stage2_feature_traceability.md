# Task 3  Feature-to-Design Traceability

The following table demonstrates how each proposed TripGoAI feature is supported by the software design. Each feature is traced to its related use case, participating classes, important methods, sequence diagram, and applicable design patterns.

| **Feature** | **Description** | **Type** | **Related Use Case** | **Classes** | **Key Methods** | **Sequence Diagram** | **Design Pattern(s)** |
|---|---|---|---|---|---|---|---|
| **F01** | User Travel Profile and Preferences | Hybrid | UC-02 Set Preferences | `TravelPlannerGUI`, `TravelAgentController`, `TravelAgent`, `MemoryManager`, `TravelProfile` | `updatePreferences()`, `savePreference()`, `getPreferences()` | SD03 Set Preferences | Facade, MVC |
| **F02** | Natural-Language Trip Planning | Hybrid | UC-01 Plan Trip | `TravelPlannerGUI`, `CommandLineInterface`, `TravelAgentController`, `TravelAgent`, `TripRequest`, `LLMClient` | `submitRequest()`, `planTrip()`, `interpretRequest()` | SD01 Plan Trip | Facade, Adapter, MVC |
| **F03** | Destination Research | Hybrid | UC-03 Research Destination | `TravelAgentController`, `TravelAgent`, `ToolManager`, `TravelTool`, `DestinationSearchTool` | `getRecommendations()`, `executeTool()`, `execute()` | SD02 Search Travel Information | Facade, Adapter |
| **F04** | Multi-Day Itinerary Generation | Hybrid | UC-01 Plan Trip | `TravelAgent`, `TripPlanner`, `PlanningStrategy`, `Itinerary`, `LLMClient` | `planTrip()`, `createItinerary()`, `createPlan()`, `generate()` | SD01 Plan Trip | Strategy, Adapter, Facade |
| **F05** | Attraction Recommendations | Hybrid | UC-04 Get Travel Recommendations | `TravelAgentController`, `TravelAgent`, `ToolManager`, `AttractionSearchTool` | `getRecommendations()`, `executeTool()`, `execute()` | SD02 Search Travel Information | Facade, Adapter |
| **F06** | Restaurant Recommendations | Hybrid | UC-04 Get Travel Recommendations | `TravelAgentController`, `TravelAgent`, `ToolManager`, `RestaurantSearchTool` | `getRecommendations()`, `executeTool()`, `execute()` | SD02 Search Travel Information | Facade, Adapter |
| **F07** | Accommodation Planning | Hybrid | UC-04 Get Travel Recommendations | `TravelAgentController`, `TravelAgent`, `ToolManager`, `AccommodationSearchTool` | `getRecommendations()`, `executeTool()`, `execute()` | SD02 Search Travel Information | Facade, Adapter |
| **F08** | Transportation Planning | Hybrid | UC-05 Plan Transportation | `TravelAgent`, `ToolManager`, `TransportationTool`, `TravelTool` | `executeTool()`, `execute()` | SD02 Search Travel Information | Facade, Adapter |
| **F09** | Budget Estimation and Tracking | Hybrid | UC-06 View Budget | `TravelAgentController`, `TripPlanner`, `BudgetManager`, `Itinerary` | `showBudget()`, `calculateTotal()`, `isWithinBudget()` | SD01 Plan Trip | Facade, Strategy |
| **F10** | Schedule Conflict Detection | Hybrid | UC-07 Check Schedule Conflicts | `TravelAgent`, `TripPlanner`, `ScheduleManager`, `Itinerary` | `checkConflicts()`, `createItinerary()` | SD01 Plan Trip | Strategy, Facade |
| **F11** | Weather-Aware Itinerary Adjustment | Hybrid | UC-08 Adjust Itinerary for Weather | `TravelAgentController`, `TravelAgent`, `ToolManager`, `WeatherTool`, `Itinerary` | `checkWeather()`, `executeTool()`, `execute()`, `modifyTrip()`, `addActivity()`, `removeActivity()` | SD04 Modify Itinerary & Weather Adjustment | Facade, Adapter |
| **F12** | Natural-Language Itinerary Editing | Hybrid | UC-09 Modify Itinerary | `TravelPlannerGUI`, `TravelAgentController`, `TravelAgent`, `LLMClient`, `Itinerary` | `modifyTrip()`, `generate()`, `addActivity()`, `removeActivity()` | SD04 Modify Itinerary & Weather Adjustment | Facade, Adapter, MVC |
| **F13** | Alternative Plan Generation | Hybrid | UC-10 Generate Alternative Plan | `TravelPlannerGUI`, `TravelAgentController`, `TravelAgent`, `TripPlanner`, `LLMClient` | `generateAlternative()`, `generate()`, `createItinerary()` | SD05 Generate Alternative Plan | Strategy, Adapter, Facade |
| **F14** | Packing List Generation | Hybrid | UC-11 Generate Packing List | `TravelAgent`, `LLMClient`, `MemoryManager`, `TravelProfile` | `generatePackingList()`, `generate()`, `getPreferences()` | Covered by AI/agent generation workflow | Adapter, Facade |

## Traceability Summary

- **F01** is represented by UC-02 and SD03 through `TravelAgent`, `MemoryManager`, and `TravelProfile`.
- **F02 and F04** are represented by UC-01 and SD01 and form the main AI-assisted trip-planning and itinerary-generation workflow.
- **F03 and F05–F08** use the tool architecture represented by SD02 to support destination research, recommendations, accommodation planning, and transportation planning.
- **F09 and F10** are supported by `BudgetManager` and `ScheduleManager` as part of the itinerary planning and validation design.
- **F11 and F12** are represented by SD04, which demonstrates natural-language itinerary modification and weather-aware adjustment.
- **F13** is represented by SD05 and demonstrates AI-assisted alternative-plan generation.
- **F14** is supported by `TravelAgent.generatePackingList()`, `LLMClient.generate()`, and stored traveler preferences through `MemoryManager`.
- The **Save & Retrieve Trip** sequence diagram additionally demonstrates persistence through `TripRepository` and `DatabaseManager`.
