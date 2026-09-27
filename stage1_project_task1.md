# Task 1: Define Your Agent Project

## Project Title: VoyageAI – AI Travel Planning Agent

VoyageAI is an AI-agent-based travel planning system designed to help users research destinations, create personalized itineraries, organize travel information, and modify travel plans. The system will provide both a graphical user interface (GUI) and a command-line interface (CLI), allowing users to access the major functionality of the application through either interface.

Unlike a basic application that only sends a user prompt to an LLM and displays the response, VoyageAI will use an agent architecture that supports reasoning, planning, memory, information retrieval, tool use, decision making, and multi-step task execution.

# 1.1 Project Description

## Problem the Project Solves

Planning a trip often requires users to search several different websites or services for destinations, attractions, restaurants, accommodation, transportation, weather, schedules, and estimated prices. The traveler must then manually combine this information into a realistic travel plan.

This process becomes more difficult when the traveler has constraints such as a limited budget, specific travel dates, dietary restrictions, preferred activities, transportation preferences, accommodation preferences, weather concerns, or limited available time.

VoyageAI solves this problem by providing one centralized travel-planning system where an AI agent can understand the user's travel requirements, retrieve relevant information, reason about multiple constraints, and generate a personalized travel plan.

## Target Users

The main users of VoyageAI are people who want assistance planning personal trips.

Potential users include:

- university students planning affordable trips;
- solo travelers;
- families planning vacations;
- tourists visiting unfamiliar destinations;
- budget-conscious travelers;
- travelers with dietary or activity preferences;
- users planning multi-day trips.

The system is intended for travelers who want a structured travel plan without having to manually organize information from many different sources.

## What the Agent Can Do

The VoyageAI agent can understand natural-language travel requests, extract important travel requirements, research destinations, retrieve travel information through tools, generate multi-day itineraries, recommend attractions and restaurants, assist with accommodation and transportation planning, estimate expenses, detect scheduling conflicts, check weather conditions, revise itineraries, generate alternative plans, and create personalized packing lists.

The agent can also maintain relevant travel preferences and trip context so that users do not have to repeat the same information during every interaction.

For example, a user may enter:

“Plan a four-day trip to Vancouver for less than $1,500. I like hiking, seafood, and museums, and I do not want activities before 9:00 AM.”

The agent can identify the destination, trip duration, budget, interests, and scheduling constraint. It can then retrieve relevant travel information, organize activities into a multi-day itinerary, estimate the cost, and verify that the itinerary does not violate important scheduling constraints.

## Why an AI Agent Is Appropriate

An AI agent is appropriate for this project because travel planning requires more than generating a single text response.

The system must understand natural-language requests, reason about several constraints, retrieve information using tools, create plans, remember relevant information, and perform multiple actions to complete complex requests.

For example, a user might say:

“Move the outdoor activities from Saturday because it is going to rain and keep the trip under the same budget.”

To complete this request, the agent may need to retrieve weather information, identify outdoor activities, find suitable alternatives, update the itinerary, check the new schedule, recalculate the budget, and return the revised plan.

This type of multi-step behavior is more appropriate for an AI agent than for a simple chatbot.

## Planned AI/LLM Model

VoyageAI plans to use an OpenAI GPT-family large language model.

The AI model will be accessed through an `LLMClient` interface so that the rest of the software is not directly dependent on one specific AI model.

The AI model will be used for tasks such as:

- understanding natural-language travel requests;
- extracting travel constraints;
- reasoning about user preferences;
- generating and revising itineraries;
- ranking recommendations;
- explaining travel options;
- interpreting itinerary modification requests;
- generating personalized packing suggestions.

## How the AI Model Interacts With the Software System

The GUI and CLI will not communicate directly with the AI model.

Instead, user requests will pass through the software architecture.

The high-level interaction will be:

**User → GUI/CLI → TravelAgentController → TravelAgent → Planner/Tools/Memory → LLMClient → Result**

`TravelAgentController` receives requests from the GUI or CLI and coordinates the application.

`TravelAgent` manages AI-related behavior such as reasoning, planning, tool selection, and natural-language interpretation.

`TripPlanner` creates and modifies travel itineraries.

`ToolManager` provides access to travel-related tools.

Possible tools include:

- `DestinationSearchTool`;
- `AttractionSearchTool`;
- `RestaurantSearchTool`;
- `AccommodationSearchTool`;
- `TransportationTool`;
- `WeatherTool`.

`MemoryManager` stores relevant travel preferences and current trip context.

`BudgetManager` calculates and tracks estimated expenses.

`ScheduleManager` checks the itinerary for scheduling conflicts.

`LLMClient` communicates with the selected AI/LLM model.

This architecture ensures that AI-generated output is combined with deterministic software components rather than being accepted directly without validation.

## Graphical User Interface

The GUI will provide a trip-planning dashboard containing the main features of the system.

The dashboard will include areas for:

- natural-language trip requests;
- user travel preferences;
- destination information;
- itinerary timeline;
- attraction recommendations;
- restaurant recommendations;
- accommodation planning;
- transportation planning;
- weather information;
- budget summary;
- itinerary editing;
- alternative plans;
- packing lists.

## Command-Line Interface

The CLI will allow users to access the major travel-planning features without using the graphical interface.

Possible commands include:

`plan-trip`

`research-destination`

`recommend-attractions`

`recommend-restaurants`

`find-accommodation`

`plan-transportation`

`show-budget`

`check-conflicts`

`check-weather`

`modify-trip`

`alternative-plan`

`packing-list`

The GUI and CLI will use the same underlying controller and software components so that application logic is not duplicated.

# 1.2 Feature Specification

VoyageAI contains fourteen meaningful features.

## F01 — User Travel Profile and Preferences

The user creates or updates a travel profile through the GUI by entering preferences such as budget level, preferred activities, dietary restrictions, accommodation preferences, transportation preferences, and preferred travel pace. The same functionality is available through the CLI. The system validates and stores the information so that the AI agent can use it when generating future recommendations and itineraries. The output is an updated travel profile available to the agent. This feature is hybrid because profile storage and validation are deterministic, while the AI uses the stored preferences during later planning. If invalid information is entered, the system asks the user to correct it while preserving previously valid preferences.

## F02 — Natural-Language Trip Planning

The user enters a destination, travel dates, budget, interests, and preferences using natural language through the GUI or CLI. The agent analyzes the request, extracts the important travel constraints, retrieves relevant information through its available tools, and creates a structured travel request that can be used to generate a trip plan. The output is a validated set of travel requirements and, when sufficient information is available, a personalized multi-day itinerary. This feature is hybrid because the AI interprets the request while deterministic validation checks the extracted information. If essential details such as the destination or dates are missing or unclear, the agent asks the user for clarification.

## F03 — Destination Research

The user selects a destination through the GUI or enters the destination through the CLI and asks the agent to research it. The agent uses its destination research tool to retrieve relevant information and passes the retrieved data to the AI model for organization and summarization. The output is a structured destination summary that can be shown to the user and used during later travel planning. This feature is hybrid because the retrieval process uses software tools while the AI interprets and summarizes the retrieved information. If destination information cannot be retrieved, the system informs the user rather than presenting unsupported information.

## F04 — Multi-Day Itinerary Generation

The user enters the destination, travel dates, budget, interests, preferences, and other constraints through the GUI or CLI. The agent analyzes these requirements, retrieves necessary travel information through its tools, and generates a day-by-day itinerary. The system then checks the proposed itinerary for scheduling conflicts and estimates the total cost before displaying it. The output is a complete multi-day itinerary containing activities and travel recommendations. This feature is hybrid because the AI performs planning and reasoning while deterministic components validate the schedule and budget. If the proposed itinerary violates important constraints, the agent attempts to revise the plan or informs the user that all constraints cannot be satisfied.

## F05 — Attraction Recommendations

The user requests attraction recommendations through the Attractions section of the GUI or through the CLI. The agent retrieves available attractions and evaluates them according to the destination, user interests, travel dates, current itinerary, and budget. The output is a collection of suitable attractions with explanations of why they may fit the user's trip. This feature is hybrid because the system retrieves real travel information while the AI evaluates and explains the recommendations. If no attractions satisfy the requested constraints, the system informs the user and may suggest changing one of the restrictions.

## F06 — Restaurant Recommendations

The user enters restaurant preferences such as location, cuisine type, dietary restrictions, and budget through the GUI or CLI. The agent retrieves restaurant information, removes options that do not satisfy important requirements, and uses the AI model to identify and explain suitable choices. The output is a list of recommended restaurants that may later be added to the itinerary. This feature is hybrid because restaurant retrieval and filtering involve deterministic software while the AI evaluates the remaining choices. If current restaurant information cannot be retrieved, the system informs the user that current recommendations are unavailable.

## F07 — Accommodation Planning

The user enters the destination, travel dates, accommodation type, preferred area, and budget through the GUI or CLI. The agent retrieves accommodation options and compares them with the user's preferences and current travel plan. The output is a collection of suitable accommodation recommendations with estimated cost information when available. This feature is hybrid because the system retrieves and filters accommodation data while the AI assists in evaluating the trade-offs between different options. If no accommodation satisfies the requirements, the agent explains which constraints are preventing a suitable match.

## F08 — Transportation Planning

The user selects two locations from the itinerary or enters an origin and destination through the GUI or CLI. The agent retrieves relevant transportation information and evaluates possible options such as walking, public transportation, taxi, rideshare, rental vehicle, or intercity transportation. The output is one or more recommended transportation options with estimated travel information. This feature is hybrid because travel data are retrieved using tools while the AI compares the options against cost, timing, and user preferences. If transportation information cannot be retrieved, the system informs the user instead of inventing current routes or schedules.

## F09 — Budget Estimation and Tracking

The user opens the Budget section of the GUI or requests a budget summary through the CLI. The system collects estimated expenses for accommodation, transportation, food, attractions, and other activities and calculates the category totals and estimated total trip cost. The output includes the total estimated cost, category totals, remaining budget, and warnings when the trip exceeds the user's budget. This feature is hybrid because all mathematical calculations are deterministic while the AI may analyze the result and suggest lower-cost alternatives. If some prices are unavailable, the system marks them as unknown or estimated instead of treating them as zero.

## F10 — Schedule Conflict Detection

The system automatically checks the itinerary whenever activities are generated, added, moved, or replaced, and the user may also request a schedule check through the GUI or CLI. The system compares activity start times, durations, and transportation requirements to identify overlapping or unrealistic schedules. The output is a list of detected conflicts together with possible recommendations for resolving them. This feature is hybrid because the conflict detection itself is deterministic while the AI may recommend alternative arrangements. If important timing information is missing, the system reports that the schedule cannot be fully validated.

## F11 — Weather-Aware Itinerary Adjustment

The user requests a weather check through the GUI or CLI. The agent retrieves weather information for the destination and travel dates, identifies activities that may be unsuitable because of the expected conditions, and modifies the itinerary when appropriate. The output is an updated weather-aware itinerary that is checked again for budget and schedule conflicts before being displayed. This feature is hybrid because the system retrieves weather information and performs deterministic validation while the AI reasons about which activities should be replaced or moved. If weather information cannot be retrieved, the original itinerary remains unchanged and the user is informed of the limitation.

## F12 — Natural-Language Itinerary Editing

The user modifies an existing itinerary by entering a natural-language instruction through the GUI or CLI, such as “move the museum to Sunday,” “make Saturday less busy,” or “replace dinner with a cheaper restaurant.” The AI agent interprets the instruction and converts it into a structured itinerary modification. The system applies the requested change, checks the resulting schedule and budget, and displays the updated itinerary. The output is a validated modified itinerary. This feature is hybrid because the AI interprets the user's request while deterministic components perform and validate the actual itinerary changes. If the instruction is ambiguous or refers to an activity that does not exist, the agent asks the user for clarification.

## F13 — Alternative Plan Generation

The user requests an alternative version of the current trip through the GUI or CLI and may specify requirements such as a lower budget, slower travel pace, or different activities. The AI agent analyzes the current itinerary and fixed constraints, retrieves additional information when necessary, and generates a different multi-day plan. The output is an alternative itinerary that is displayed separately so the original plan is not automatically replaced. This feature is hybrid because the AI creates the alternative plan while deterministic components verify its budget and schedule. If no feasible alternative satisfies the required constraints, the agent explains which constraints are preventing another valid plan.

## F14 — Packing List Generation

The user selects Generate Packing List through the GUI or uses the corresponding CLI command. The agent analyzes the destination, travel dates, duration, planned activities, user preferences, and available weather information to create a personalized packing list. The output is a categorized list of recommended items for the trip. This feature is hybrid because the AI generates personalized packing recommendations while the system supplies deterministic trip and weather data. If weather information is unavailable, the agent can still produce a general packing list but informs the user that the recommendations are not weather-specific.
