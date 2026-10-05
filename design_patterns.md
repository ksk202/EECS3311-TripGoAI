
TripGoAI uses five design patterns to improve flexibility, maintainability, separation of concerns, and extensibility. Each pattern addresses a specific design problem in the system.

## 1. Strategy Pattern

### Design Problem

TripGoAI may need to generate travel plans using different planning approaches. For example, one traveler may prioritize staying within a strict budget, while another may prefer a more balanced itinerary. Placing all planning approaches directly inside `TripPlanner` would make the class difficult to modify and extend.

### Participating Classes and Roles

- `PlanningStrategy` : defines the common interface for itinerary-planning strategies through `createPlan(request: TripRequest)`.
- `BudgetStrategy` : implements `PlanningStrategy` and creates itineraries that prioritize budget constraints.
- `BalancedStrategy` : implements `PlanningStrategy` and creates itineraries that balance different travel requirements.
- `TripPlanner` : maintains a reference to a `PlanningStrategy` and can change the active strategy using `setStrategy()`.

### Why the Pattern Is Appropriate

The Strategy pattern allows the planning algorithm to vary independently from `TripPlanner`. New planning approaches can be introduced without rewriting the main planning component.

### Without the Pattern

Without Strategy, `TripPlanner` would require conditional logic for every planning approach. Adding new planning styles would make the class larger, harder to maintain, and more difficult to extend.



## 2. Adapter Pattern

### Design Problem

TripGoAI needs to communicate with an external AI/LLM provider. The external provider's API should not be directly coupled to the rest of the application because the AI provider or API implementation may change.

### Participating Classes and Roles

- `LLMClient` : defines the interface expected by TripGoAI for AI operations such as generating responses and extracting a `TripRequest`.
- `OpenAIAdapter` : implements `LLMClient` and translates TripGoAI requests into operations supported by the selected OpenAI model.

### Why the Pattern Is Appropriate

The Adapter pattern isolates provider-specific AI communication from the rest of the application. Components such as `TravelAgent` depend on `LLMClient` rather than directly depending on a specific external AI API.

### Without the Pattern

Without Adapter, `TravelAgent` and other components could become directly dependent on provider-specific API details. Changing the AI provider or API implementation would require modifications throughout the system.



## 3. Singleton Pattern

### Design Problem

TripGoAI requires centralized management of its database connection. Creating multiple independent database managers could result in unnecessary connection objects and inconsistent connection management.

### Participating Classes and Roles

- `DatabaseManager` : maintains the shared database-management instance.
- `DatabaseManager.getInstance()` : provides access to that shared instance.
- `TripRepository` : uses `DatabaseManager` when storing and retrieving trip information.

### Why the Pattern Is Appropriate

The Singleton pattern provides one shared access point for database connection management throughout the application.

### Without the Pattern

Without Singleton, different parts of the system could create separate `DatabaseManager` instances, making database connection management less consistent and potentially increasing resource usage.



## 4. Facade Pattern

### Design Problem

The GUI and CLI need access to many capabilities of TripGoAI, including trip planning, recommendations, itinerary modification, weather checking, budget information, AI processing, persistence, and other services. Requiring the interfaces to communicate directly with every subsystem would create unnecessary coupling.

### Participating Classes and Roles

- `TravelAgentController` : acts as the facade and provides simplified operations such as `planTrip()`, `getRecommendations()`, `modifyTrip()`, `checkWeather()`, and `showBudget()`.
- `TravelPlannerGUI` : uses the facade to access application functionality.
- `CommandLineInterface` : uses the same facade for CLI requests.
- `TravelAgent` and `TripRepository` — represent major underlying components coordinated through the controller.

### Why the Pattern Is Appropriate

The Facade pattern gives both user interfaces a simplified entry point into the application's more complex subsystems. The GUI and CLI do not need to understand how AI processing, planning, tools, memory, validation, and persistence are internally coordinated.

### Without the Pattern

Without Facade, the GUI and CLI would need direct dependencies on many internal components. This would increase coupling, duplicate coordination logic, and make changes to the internal architecture more likely to affect the user interfaces.



## 5. MVC Pattern

### Design Problem

TripGoAI needs to separate user-interface responsibilities from application-control logic and travel-related data and services. Mixing these responsibilities would make the system harder to maintain and would make supporting both GUI and CLI interactions more difficult.

### Participating Classes and Roles

- `TravelPlannerGUI` : acts as a View by collecting user input and displaying results.
- `TravelAgentController` : acts as the Controller by receiving user actions and coordinating application operations.
- `Trip`, `Itinerary`, `Activity`, `TravelProfile`, and related service/domain components — form the Model side of the architecture by representing and managing application data and travel-related behavior.
- `CommandLineInterface` : provides an additional user-interface boundary that accesses the same application controller.

### Why the Pattern Is Appropriate

MVC separates presentation from application coordination and domain logic. This supports cleaner organization and allows the GUI and CLI to reuse the same underlying functionality.

### Without the Pattern

Without MVC-style separation, interface code, control logic, and domain behavior could become tightly coupled. Changes to the GUI could affect business logic, and supporting multiple interfaces would require more duplicated code.
