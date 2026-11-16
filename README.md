# TripGoAI  AI Travel Planning Agent

## Project Description

TripGoAI is a Java-based AI travel planning application developed for EECS 3311 at York University.

It helps users plan trips using AI and provides both a GUI and CLI.

## Features

- AI-powered trip planning
- Destination and attraction recommendations
- Restaurant and hotel suggestions
- Budget and schedule checking
- Weather forecasts
- Driving-route planning
- Itinerary editing with Undo
- Alternative trip plans
- Packing list generation
- Save and load trips

## Technologies

- Java
- Eclipse IDE
- OpenAI API
- OpenStreetMap and OSRM
- GitHub

## How to Run

1. Download or clone this repository.
2. Open the Java project in Eclipse.
3. Install any required libraries and configure the Java build path.
4. Open `.env.example` and use it as a guide to configure your own OpenAI API key. Never commit your real key.
5. Run the GUI's main class in Eclipse to start the graphical application.
6. Alternatively, run the CLI's main class to use the console version.

**Requirements:** Java JDK, Eclipse IDE, an internet connection, and an OpenAI API key for AI features.

## Example Usage

1. Launch TripGoAI.
2. Enter a destination, travel dates, and budget.
3. Generate an itinerary.
4. Review activities, costs, and schedule.
5. Save your trip or modify it using AI.

## Design Patterns

TripGoAI uses five main design patterns:

- Facade
- Strategy
- Adapter
- Singleton
- Command

## Known Limitations

- Transportation supports driving only.
- Some activity prices and details may be unavailable.
- Restaurant and hotel retrieval tools require further verification.
- Automatic weather-related activity replacement is not fully verified.


