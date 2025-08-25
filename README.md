# MBTA Simulator

A multithreaded Java simulation of the Massachusetts Bay Transportation Authority (MBTA) subway system.  
Models trains and passengers moving through stations in real-time, with careful synchronization to ensure correctness and deadlock-free execution.

## Key Features

- **Threaded Simulation**
  - Each passenger and train runs in its own thread.
  - Real-time interaction between threads models boarding, deboarding, and train movement.

- **Safe Shared State**
  - Central `MBTA` instance stores all state in `ConcurrentHashMap`s.
  - Synchronization on station objects prevents race conditions when multiple threads update shared data.

- **Concurrency & Deadlock Avoidance**
  - Ordered lock acquisition across stations ensures no circular waiting.
  - Uses `synchronized`, `wait()`, and `notifyAll()` for coordination.
  - Train threads release locks during short sleep intervals to allow other actions to proceed.

- **Passenger Logic**
  - Passengers decide whether to board, wait, or deboard based on train and station state.
  - Multiple cases handled, from reaching a destination to waiting for the correct train.

- **Train Logic**
  - Trains coordinate movement based on station occupancy.
  - Movement triggers `notifyAll()` to wake waiting passengers and trains.

## Technical Highlights

- Java concurrency primitives: `synchronized`, `wait()`, `notifyAll()`
- Thread-safe collections: `ConcurrentHashMap`
- Deadlock prevention through ordered locking
- Object-oriented design with trains, passengers, and stations sharing a central state

## Why It Stands Out

- Demonstrates ability to design and implement **complex concurrent systems** in Java  
- Showcases **race condition prevention**, **deadlock avoidance**, and **real-world systems modeling**  
- Strong example of applying **software engineering best practices** to a challenging synchronization problem

## Running the Project

```bash
git clone https://github.com/ratva/MBTA-Simulator.git
cd MBTA-Simulator
javac *.java
java MBTA
