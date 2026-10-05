# 2.2 Use-Case Descriptions

## UC-01 — Plan Trip

**Use Case ID:** UC-01  
**Use Case Name:** Plan Trip  
**Actor(s):** Traveler  
**Goal:** Create a personalized travel plan.

**Preconditions:**  
The Traveler has the required trip information.

**Trigger:**  
The Traveler chooses to plan a new trip.

**Main Success Scenario:**
1. Traveler enters trip details.
2. TripGoAI receives the trip information.
3. TripGoAI searches for relevant travel options.
4. TripGoAI generates an itinerary.
5. The itinerary is displayed to the Traveler.

**Alternative/Exception Flows:**  
If required information is missing, the system asks the Traveler to provide it. If an external service fails, the system displays an error.

**Postconditions:**  
A travel itinerary is generated.

**Related Feature(s):**  
Trip planning and AI itinerary generation.

---

## UC-02 — Set Preferences

**Use Case ID:** UC-02  
**Use Case Name:** Set Preferences  
**Actor(s):** Traveler  
**Goal:** Set travel preferences for the trip.

**Preconditions:**  
The Traveler is using TripGoAI.

**Trigger:**  
The Traveler chooses to set or change preferences.

**Main Success Scenario:**
1. Traveler enters travel preferences.
2. TripGoAI receives the preferences.
3. TripGoAI stores the preferences.
4. The preferences are used for trip planning.

**Alternative/Exception Flows:**  
If a preference is invalid, the system asks the Traveler to correct it.

**Postconditions:**  
The Traveler's preferences are available for itinerary generation.

**Related Feature(s):**  
Travel preference management.

---

## UC-03 — View Itinerary

**Use Case ID:** UC-03  
**Use Case Name:** View Itinerary  
**Actor(s):** Traveler 
**Goal:** View the generated travel itinerary and route information.

**Preconditions:**  
An itinerary has been generated.

**Trigger:**  
The Traveler chooses to view the itinerary.

**Main Success Scenario:**
1. Traveler requests the itinerary.
2. TripGoAI retrieves the itinerary.
3. TripGoAI obtains route/map data when needed.
4. TripGoAI displays the itinerary.

**Alternative/Exception Flows:**  
If no itinerary exists, the system informs the Traveler. If map data is unavailable, the itinerary is displayed without route information.

**Postconditions:**  
The Traveler can view the itinerary.

**Related Feature(s):**  
Itinerary viewing and route/map information.

---

## UC-04 — Modify Itinerary

**Use Case ID:** UC-04  
**Use Case Name:** Modify Itinerary  
**Actor(s):** Traveler  
**Goal:** Change an existing itinerary.

**Preconditions:**  
An itinerary has been generated.

**Trigger:**  
The Traveler chooses to modify the itinerary.

**Main Success Scenario:**
1. Traveler selects the itinerary to modify.
2. Traveler makes the desired changes.
3. TripGoAI updates the itinerary.
4. TripGoAI displays the updated itinerary.

**Alternative/Exception Flows:**  
If a requested change is invalid, the system asks the Traveler to enter another change.

**Postconditions:**  
The itinerary contains the Traveler's changes.

**Related Feature(s):**  
Itinerary modification.

---

## UC-05 — Save Trip

**Use Case ID:** UC-05  
**Use Case Name:** Save Trip  
**Actor(s):** Traveler  
**Goal:** Save a trip for later use.

**Preconditions:**  
A trip or itinerary has been created.

**Trigger:**  
The Traveler chooses to save the trip.

**Main Success Scenario:**
1. Traveler selects Save Trip.
2. TripGoAI stores the trip information.
3. TripGoAI confirms that the trip was saved.

**Alternative/Exception Flows:**  
If the trip cannot be saved, the system displays an error.

**Postconditions:**  
The trip is stored in the system.

**Related Feature(s):**  
Trip storage.

---

## UC-06 — View Saved Trips

**Use Case ID:** UC-06  
**Use Case Name:** View Saved Trips  
**Actor(s):** Traveler  
**Goal:** View previously saved trips.

**Preconditions:**  
At least one trip has been saved.

**Trigger:**  
The Traveler chooses to view saved trips.

**Main Success Scenario:**
1. Traveler requests saved trips.
2. TripGoAI retrieves the saved trips.
3. TripGoAI displays the saved trips.

**Alternative/Exception Flows:**  
If there are no saved trips, the system informs the Traveler.

**Postconditions:**  
The Traveler can view previously saved trips.

**Related Feature(s):**  
Saved trip management.

---

## UC-07 — Generate AI Itinerary

**Use Case ID:** UC-07  
**Use Case Name:** Generate AI Itinerary  
**Actor(s):** AI Service  
**Goal:** Generate a personalized itinerary using AI.

**Preconditions:**  
Trip details are available.

**Trigger:**  
TripGoAI requests an AI-generated itinerary.

**Main Success Scenario:**
1. TripGoAI sends trip information to the AI Service.
2. The AI Service processes the information.
3. The AI Service generates itinerary information.
4. TripGoAI receives the generated itinerary.

**Alternative/Exception Flows:**  
If the AI Service is unavailable, TripGoAI displays an error.

**Postconditions:**  
An AI-generated itinerary is available.

**Related Feature(s):**  
AI itinerary generation.

---

## UC-08 — Get Route/Map Data

**Use Case ID:** UC-08  
**Use Case Name:** Get Route/Map Data  
**Actor(s):** Maps API  
**Goal:** Obtain route and map information for a trip.

**Preconditions:**  
The system has destination or itinerary information.

**Trigger:**  
TripGoAI requires route or map information.

**Main Success Scenario:**
1. TripGoAI sends a request to the Maps API.
2. The Maps API processes the request.
3. The Maps API returns route/map data.
4. TripGoAI uses the data in the itinerary.

**Alternative/Exception Flows:**  
If the Maps API is unavailable, TripGoAI continues without map information or displays an error.

**Postconditions:**  
Route/map information is available to TripGoAI.

**Related Feature(s):**  
Route and map information.

---

## UC-09 — Search Travel Options

**Use Case ID:** UC-09  
**Use Case Name:** Search Travel Options  
**Actor(s):** Travel Data API  
**Goal:** Find relevant travel options for the trip.

**Preconditions:**  
Trip details are available.

**Trigger:**  
TripGoAI needs travel information for trip planning.

**Main Success Scenario:**
1. TripGoAI sends trip criteria to the Travel Data API.
2. The Travel Data API searches for relevant options.
3. The Travel Data API returns travel information.
4. TripGoAI uses the information for trip planning.

**Alternative/Exception Flows:**  
If no results are found, TripGoAI informs the Traveler. If the Travel Data API is unavailable, the system displays an error.

**Postconditions:**  
Relevant travel information is available to TripGoAI.

**Related Feature(s):**  
Travel information search.
