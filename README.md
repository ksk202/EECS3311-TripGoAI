# TripGoAI  AI Travel Planning Agent

## Project Description

TripGoAI is a Java-based AI travel planning application 

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


## Standalone CLI and API Support

TripGoAI supports a graphical user interface (GUI), an interactive command-line interface (CLI), file-based CLI input, and HTTP API requests.

### Requirements

- Java 17 or newer
- `TripGoAI.jar`
- OpenAI API key for AI-powered requests

### Interactive GUI/CLI

Run the application:

```bash
java -jar TripGoAI.jar
```

Choose an interface:

1. Graphical User Interface
2. Command Line Interface
3. Both GUI and CLI

### Command-Line Interface

Start the CLI directly:

```bash
java -jar TripGoAI.jar -cli
```

### File Input

Run CLI commands from a text file:

```bash
java -jar TripGoAI.jar -cli -input task.txt
```

Example `task.txt`:

```text
5
food
Italian
6
food
17
```

The example saves a preference, retrieves it, and exits.

### HTTP API Input

Start the local API server:

```bash
java -jar TripGoAI.jar -api
```

Check server availability:

```bash
curl http://127.0.0.1:8080/health
```

Submit a travel-related AI request:

```bash
curl -X POST http://127.0.0.1:8080/task -H "Content-Type: text/plain" --data "Suggest three attractions to visit in Rome"
```

The API accepts plain-text requests and returns AI-generated text responses.

### OpenAI API Key

Before starting TripGoAI, set the `OPENAI_API_KEY` environment variable.

Windows Command Prompt:

```bat
set "OPENAI_API_KEY=YOUR_API_KEY"
```

Replace the placeholder with your own API key. 

### Supported Command-Line Arguments

| Argument | Description |
|---|---|
| No arguments | Display GUI/CLI selection |
| `-cli` | Start the interactive CLI |
| `-cli -input task.txt` | Read commands from a text file |
| `-LLMAPI` | Alternative CLI launch option |
| `-api` | Start the local HTTP API server |
| `-api -port 8081` | Start the API using port 8081 |
| `-help` | Display available commands |



