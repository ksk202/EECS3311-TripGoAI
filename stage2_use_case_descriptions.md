# Use-Case Descriptions

## UC-01 : Plan Trip

**Use Case ID:** UC-01

**Use Case Name:** Plan Trip

**Actor(s):** Traveler, AI Service

**Goal:** Create a personalized multi-day travel plan based on the Traveler's trip details and preferences.

**Preconditions:**
- The Traveler has the required trip information.
- TripGoAI is available to process the request.

**Trigger:**
The Traveler chooses to plan a new trip.

**Main Success Scenario:**
1. The Traveler enters the destination, travel dates, budget, and preferences.
2. TripGoAI receives and interprets the trip request.
3. TripGoAI obtains relevant travel information and recommendations.
4. TripGoAI uses the AI Service to assist with generating a personalized itinerary.
5. TripGoAI organizes the selected activities into a multi-day itinerary.
6. TripGoAI checks the itinerary for schedule conflicts.
7. TripGoAI presents the completed travel plan to the Traveler.

**Alternative/Exception Flows:**
- If required trip information is missing, TripGoAI asks the Traveler to provide it.
- If no suitable travel options are found, TripGoAI informs the Traveler and allows the criteria to be changed.
- If the AI Service is unavailable, TripGoAI informs the Traveler that the plan cannot be fully generated.
- If schedule conflicts are detected, TripGoAI adjusts the itinerary or informs the Traveler.

**Postconditions:**
A personalized multi-day travel itinerary is generated and available to the Traveler.

**Related Feature(s):**
- F02 : Natural-Language Trip Planning
- F04 : Multi-Day Itinerary Generation



## UC-02 : Set Preferences

**Use Case ID:** UC-02

**Use Case Name:** Set Preferences

**Actor(s):** Traveler

**Goal:** Store or update the Traveler's preferences so TripGoAI can personalize travel plans and recommendations.

**Preconditions:**
- The Traveler is using TripGoAI.

**Trigger:**
The Traveler chooses to enter or modify travel preferences.

**Main Success Scenario:**
1. The Traveler enters travel preferences.
2. TripGoAI receives the preferences.
3. TripGoAI validates the provided information.
4. TripGoAI stores the preferences in the Traveler's travel profile.
5. TripGoAI uses the stored preferences when generating future plans and recommendations.

**Alternative/Exception Flows:**
- If a preference is invalid or unsupported, TripGoAI asks the Traveler to correct it.
- If no preferences are provided, TripGoAI can continue using default planning options.

**Postconditions:**
The Traveler's preferences are stored and available for future planning and personalization.

**Related Feature(s):**
- F01  User Travel Profile and Preferences



## UC-03 — Research Destination

**Use Case ID:** UC-03

**Use Case Name:** Research Destination

**Actor(s):** Traveler, Travel Data API

**Goal:** Retrieve useful information about a destination before or during trip planning.

**Preconditions:**
- The Traveler has specified a destination.
- The Travel Data API is available.

**Trigger:**
The Traveler requests information about a destination.

**Main Success Scenario:**
1. The Traveler enters or selects a destination.
2. TripGoAI receives the destination research request.
3. TripGoAI sends the relevant search criteria to the Travel Data API.
4. The Travel Data API returns destination information.
5. TripGoAI processes and organizes the retrieved information.
6. TripGoAI presents relevant destination information to the Traveler.

**Alternative/Exception Flows:**
- If the destination cannot be found, TripGoAI asks the Traveler to verify the destination.
- If no relevant information is returned, TripGoAI informs the Traveler.
- If the Travel Data API is unavailable, TripGoAI displays an appropriate error.

**Postconditions:**
Relevant destination information is available to the Traveler and can be used during trip planning.

**Related Feature(s):**
- F03 : Destination Research

---

## UC-04 : Get Travel Recommendations

**Use Case ID:** UC-04

**Use Case Name:** Get Travel Recommendations

**Actor(s):** Traveler, AI Service, Travel Data API

**Goal:** Provide personalized recommendations for attractions, restaurants, and accommodations.

**Preconditions:**
- The Traveler has provided a destination.
- Relevant trip information or preferences are available.

**Trigger:**
The Traveler requests travel recommendations or recommendations are required while planning a trip.

**Main Success Scenario:**
1. The Traveler requests recommendations.
2. TripGoAI determines the requested recommendation type.
3. TripGoAI retrieves relevant options from the Travel Data API.
4. TripGoAI considers the Traveler's preferences, destination, budget, and trip details.
5. The AI Service helps TripGoAI evaluate and personalize the available options.
6. TripGoAI presents suitable recommendations to the Traveler.

**Alternative/Exception Flows:**
- If no suitable options are found, TripGoAI informs the Traveler and allows the search criteria to be changed.
- If the Travel Data API is unavailable, TripGoAI displays an error.
- If the AI Service is unavailable, TripGoAI informs the Traveler that personalized recommendations may not be available.

**Postconditions:**
The Traveler receives relevant travel recommendations.

**Related Feature(s):**
- F05  Attraction Recommendations
- F06  Restaurant Recommendations
- F07  Accommodation Planning

---

## UC-05 : Plan Transportation

**Use Case ID:** UC-05

**Use Case Name:** Plan Transportation

**Actor(s):** Traveler, Maps API

**Goal:** Provide transportation and route information for travel between locations in the itinerary.

**Preconditions:**
- The Traveler has provided locations or an itinerary containing locations.
- The Maps API is available.

**Trigger:**
The Traveler requests transportation information or TripGoAI needs transportation information for a trip.

**Main Success Scenario:**
1. The Traveler requests transportation planning.
2. TripGoAI identifies the relevant origin and destination locations.
3. TripGoAI sends the location information to the Maps API.
4. The Maps API returns route and transportation information.
5. TripGoAI evaluates the returned information.
6. TripGoAI presents suitable transportation information to the Traveler.

**Alternative/Exception Flows:**
- If a location cannot be identified, TripGoAI asks the Traveler to provide or correct it.
- If route information is unavailable, TripGoAI informs the Traveler.
- If the Maps API is unavailable, TripGoAI displays an appropriate error.

**Postconditions:**
Transportation information is available for the Traveler's trip.

**Related Feature(s):**
- F08  Transportation Planning

---

## UC-06 : View Budget

**Use Case ID:** UC-06

**Use Case Name:** View Budget

**Actor(s):** Traveler

**Goal:** View the estimated cost of the trip and determine whether the itinerary remains within the specified budget.

**Preconditions:**
- A trip or itinerary exists.
- Cost information is available for at least some trip components.

**Trigger:**
The Traveler chooses to view the trip budget.

**Main Success Scenario:**
1. The Traveler requests budget information.
2. TripGoAI retrieves the current itinerary and available cost estimates.
3. TripGoAI calculates the estimated total trip cost.
4. TripGoAI compares the estimated cost with the Traveler's budget limit.
5. TripGoAI displays the estimated total and budget status to the Traveler.

**Alternative/Exception Flows:**
- If some cost information is unavailable, TripGoAI calculates an estimate using the available information and identifies missing estimates.
- If the estimated cost exceeds the budget, TripGoAI informs the Traveler.
- If no budget limit has been provided, TripGoAI displays the estimated cost without a budget comparison.

**Postconditions:**
The Traveler can view the estimated trip cost and current budget status.

**Related Feature(s):**
- F09  Budget Estimation and Tracking

---

## UC-07 : Check Schedule Conflicts

**Use Case ID:** UC-07

**Use Case Name:** Check Schedule Conflicts

**Actor(s):** Traveler

**Goal:** Detect scheduling conflicts within the travel itinerary.

**Preconditions:**
- An itinerary exists or is being generated.
- Activities contain sufficient scheduling information.

**Trigger:**
The Traveler requests a conflict check or TripGoAI checks the schedule while generating or modifying an itinerary.

**Main Success Scenario:**
1. TripGoAI retrieves the activities in the itinerary.
2. TripGoAI compares activity dates and times.
3. TripGoAI checks for overlapping or incompatible activities.
4. TripGoAI identifies any schedule conflicts.
5. TripGoAI reports the results to the Traveler.
6. If necessary, the itinerary can be adjusted to resolve the conflict.

**Alternative/Exception Flows:**
- If scheduling information is missing, TripGoAI identifies the incomplete activity information.
- If a conflict cannot be automatically resolved, TripGoAI informs the Traveler and allows the itinerary to be modified.
- If no conflicts are detected, TripGoAI confirms that the schedule is valid.

**Postconditions:**
Schedule conflicts are identified so the itinerary can remain feasible.

**Related Feature(s):**
- F10  Schedule Conflict Detection



## UC-08 : Adjust Itinerary for Weather

**Use Case ID:** UC-08

**Use Case Name:** Adjust Itinerary for Weather

**Actor(s):** Traveler, Weather API, AI Service

**Goal:** Adjust itinerary activities when weather conditions make the existing plan unsuitable.

**Preconditions:**
- An itinerary exists.
- Destination and travel date information are available.

**Trigger:**
The Traveler requests a weather check or weather information indicates that itinerary changes may be necessary.

**Main Success Scenario:**
1. TripGoAI identifies the destination, dates, and weather-sensitive itinerary activities.
2. TripGoAI requests relevant weather information from the Weather API.
3. The Weather API returns weather information for the trip.
4. TripGoAI determines whether planned activities may be affected.
5. The AI Service assists with selecting appropriate replacements or adjustments when necessary.
6. TripGoAI updates or proposes changes to the affected itinerary.
7. TripGoAI presents the weather-aware itinerary adjustments to the Traveler.

**Alternative/Exception Flows:**
- If weather data is unavailable, TripGoAI informs the Traveler and keeps the existing itinerary.
- If no activities are affected by the weather, TripGoAI informs the Traveler that no adjustment is necessary.
- If no suitable replacement activity is available, TripGoAI informs the Traveler.
- If the AI Service is unavailable, TripGoAI informs the Traveler that automatic adjustment may not be available.

**Postconditions:**
The itinerary reflects weather conditions when appropriate, or the Traveler is informed that no adjustment was necessary.

**Related Feature(s):**
- F11  Weather-Aware Itinerary Adjustment



## UC-09 : Modify Itinerary

**Use Case ID:** UC-09

**Use Case Name:** Modify Itinerary

**Actor(s):** Traveler, AI Service

**Goal:** Modify an existing itinerary using natural-language instructions.

**Preconditions:**
- An itinerary has already been generated.

**Trigger:**
The Traveler requests a change to the existing itinerary.

**Main Success Scenario:**
1. The Traveler enters a natural-language modification request.
2. TripGoAI sends the instruction for AI interpretation.
3. The AI Service helps interpret the requested change.
4. TripGoAI identifies the affected itinerary activities.
5. TripGoAI applies the requested modification.
6. TripGoAI checks that the modified itinerary remains valid.
7. TripGoAI displays the updated itinerary to the Traveler.

**Alternative/Exception Flows:**
- If the request is unclear, TripGoAI asks the Traveler for clarification.
- If the requested modification cannot be performed, TripGoAI informs the Traveler.
- If the modification creates a schedule conflict, TripGoAI informs the Traveler or adjusts the itinerary.
- If the AI Service is unavailable, TripGoAI informs the Traveler that the natural-language request cannot be interpreted.

**Postconditions:**
The itinerary contains the accepted modifications.

**Related Feature(s):**
- F12  Natural-Language Itinerary Editing



## UC-10 : Generate Alternative Plan

**Use Case ID:** UC-10

**Use Case Name:** Generate Alternative Plan

**Actor(s):** Traveler, AI Service

**Goal:** Generate an alternative travel plan while preserving the Traveler's major trip constraints.

**Preconditions:**
- Trip details or an existing itinerary are available.
- The Traveler's major constraints are known.

**Trigger:**
The Traveler requests an alternative travel plan.

**Main Success Scenario:**
1. The Traveler requests an alternative plan.
2. TripGoAI retrieves the existing trip details, preferences, and constraints.
3. TripGoAI determines which parts of the plan can be changed.
4. The AI Service assists with generating alternative activities or arrangements.
5. TripGoAI constructs an alternative itinerary.
6. TripGoAI validates the alternative plan against relevant budget and scheduling constraints.
7. TripGoAI presents the alternative plan to the Traveler.

**Alternative/Exception Flows:**
- If no suitable alternatives are available, TripGoAI informs the Traveler.
- If the alternative exceeds important constraints, TripGoAI attempts another alternative or informs the Traveler.
- If the AI Service is unavailable, TripGoAI informs the Traveler that an alternative plan cannot be generated.

**Postconditions:**
An alternative itinerary is generated while preserving the major trip constraints where possible.

**Related Feature(s):**
- F13  Alternative Plan Generation



## UC-11 : Generate Packing List

**Use Case ID:** UC-11

**Use Case Name:** Generate Packing List

**Actor(s):** Traveler, AI Service

**Goal:** Generate a personalized packing list based on the Traveler's trip information.

**Preconditions:**
- Basic trip information such as destination, dates, and planned activities is available.

**Trigger:**
The Traveler requests a packing list.

**Main Success Scenario:**
1. The Traveler requests a packing list.
2. TripGoAI retrieves the destination, travel dates, itinerary, and available preferences.
3. TripGoAI prepares the relevant trip context.
4. The AI Service generates suitable packing suggestions based on the trip.
5. TripGoAI organizes the suggestions into a packing list.
6. TripGoAI displays the packing list to the Traveler.

**Alternative/Exception Flows:**
- If some trip information is missing, TripGoAI generates the list using the available information or asks the Traveler for additional details.
- If weather information is available, TripGoAI can use it to improve the packing suggestions.
- If the AI Service is unavailable, TripGoAI informs the Traveler that the packing list cannot be generated.

**Postconditions:**
A personalized packing list is available to the Traveler.

**Related Feature(s):**
- F14  Packing List Generation
