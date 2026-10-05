

Feature-to-Design Traceability



| Feature | Description | Type | Related Use Case | Classes | Key Methods | Sequence Diagram | Design Pattern(s) |
|---|---|---|---|---|---|---|---|
| **F01** | User Travel Profile and Preferences | Hybrid | UC-02 Set Preferences | `TravelProfile`, `TravelAgent`, `MemoryManager` | `savePreference()`, `getPreferences()` | SD03 Modify Trip & Preferences | Singleton, Facade |
| **F02** | Natural-Language Trip Planning | Hybrid | UC-01 Plan Trip | `TravelPlannerGUI`, `CommandLineInterface`, `TravelAgentController`, `TravelAgent`, `TripRequest`, `LLMClient` | `submitRequest()`, `planTrip()`, `interpretRequest()`, `extractTripRequest()` | SD01 Plan Trip & AI Generation | Facade, Adapter |
| **F03** | Destination Research | Hybrid | UC-09 Search Travel Options | `TravelAgentController`, `TravelAgent`, `ToolManager`, `TravelTool`, `LLMClient` | `getRecommendations()`, `executeTool()`, `execute()`, `generate()` | SD02 Search Travel Information | Facade, Adapter |
| **F04** | Multi-Day Itinerary Generation | Hybrid | UC-01 Plan Trip; UC-07 Generate AI Itinerary | `TravelAgent`, `TripPlanner`, `PlanningStrategy`, `Itinerary`, `BudgetManager`, `ScheduleManager`, `LLMClient` | `planTrip()`, `createItinerary()`, `generate()`, `calculateTotal()`, `checkConflicts()` | SD01 Plan Trip & AI Generation | Strategy, Adapter, Facade |
| **F05** | Attraction Recommendations | Hybrid | UC-09 Search Travel Options | `TravelAgentController`, `TravelAgent`, `ToolManager`, `TravelTool` | `getRecommendations()`, `executeTool()`, `execute()` | SD02 Search Travel Information | Facade, Adapter |
| **F06** | Restaurant Recommendations | Hybrid | UC-09 Search Travel Options | `TravelAgentController`, `TravelAgent`, `ToolManager`, `TravelTool` | `getRecommendations()`, `executeTool()`, `execute()` | SD02 Search Travel Information | Facade, Adapter |
| **F07** | Accommodation Planning | Hybrid | UC-09 Search Travel Options | `TravelAgentController`, `TravelAgent`, `ToolManager`, `TravelTool` | `getRecommendations()`, `executeTool()`, `execute()` | SD02 Search Travel Information | Facade, Adapter |
| **F08** | Transportation Planning | Hybrid | UC-08 Get Route/Map Data; UC-09 Search Travel Options | `TravelAgent`, `ToolManager`, `TravelTool` | `executeTool()`, `execute()` | SD02 Search Travel Information | Facade, Adapter |
| **F09** | Budget Estimation and Tracking | Hybrid | UC-01 Plan Trip; UC-04 Modify Itinerary | `TravelAgentController`, `TripPlanner`, `BudgetManager`, `Itinerary` | `showBudget()`, `calculateTotal()`, `isWithinBudget()` | SD01 Plan Trip & AI Generation; SD03 Modify Trip & Preferences | Facade, Strategy |
| **F10** | Schedule Conflict Detection | Hybrid | UC-01 Plan Trip; UC-04 Modify Itinerary | `TravelAgent`, `TripPlanner`, `ScheduleManager`, `Itinerary` | `checkConflicts()`, `createItinerary()`, `modifyTrip()` | SD01 Plan Trip & AI Generation; SD03 Modify Trip & Preferences | Strategy, Facade |
| **F11** | Weather-Aware Itinerary Adjustment | Hybrid | UC-09 Search Travel Options; UC-04 Modify Itinerary | `TravelAgentController`, `TravelAgent`, `ToolManager`, `TravelTool`, `Itinerary` | `checkWeather()`, `executeTool()`, `execute()`, `modifyTrip()` | SD02 Search Travel Information; SD03 Modify Trip & Preferences | Facade, Adapter |
| **F12** | Natural-Language Itinerary Editing | Hybrid | UC-04 Modify Itinerary | `TravelPlannerGUI`, `TravelAgentController`, `TravelAgent`, `Itinerary` | `modifyTrip()`, `addActivity()`, `removeActivity()` | SD03 Modify Trip & Preferences | Facade, Command |
| **F13** | Alternative Plan Generation | Hybrid | UC-01 Plan Trip; UC-07 Generate AI Itinerary | `TravelAgent`, `TripPlanner`, `PlanningStrategy`, `BudgetManager`, `ScheduleManager`, `LLMClient` | `planTrip()`, `createItinerary()`, `setStrategy()`, `generate()` | SD01 Plan Trip & AI Generation | Strategy, Adapter |
| **F14** | Packing List Generation | Hybrid | UC-09 Search Travel Options | `TravelAgent`, `ToolManager`, `LLMClient`, `MemoryManager` | `executeTool()`, `generate()`, `getPreferences()` | SD02 Search Travel Information | Facade, Adapter |



