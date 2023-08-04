# Concurrent Rubik's Cube Simulator

<img src="https://upload.wikimedia.org/wikipedia/commons/thumb/4/44/Scramble.svg/1200px-Scramble.svg.png" alt="[Image of Rubik's cube]" width=200/>

Welcome to the Concurrent Rubik's Cube Simulator repository! This project is an implementation of a Rubik's Cube simulator that allows concurrent rotations of the sides of the cube using a specific and delicate thread synchronization algorithm. The goal of this simulator is to demonstrate the power and complexity of managing concurrent operations on a Rubik's Cube, providing a hands-on experience for users to understand the challenges involved in concurrent programming.

## Features

- **Concurrent Rotations:** The simulator supports concurrent rotations of the Rubik's Cube sides along the same axis. This allows for simultaneous execution of multiple rotations, mimicking real-world scenarios where different parts of the cube can be manipulated independently.

- **Thread Synchronization Algorithm:** The most critical aspect of this project is the specialized thread synchronization algorithm. This algorithm ensures that multiple threads can perform rotations safely and correctly without interfering with each other's operations, preserving the integrity of the cube's state at all times.

- **Visualization:** The simulator offers a graphical user interface (GUI) that provides a visual representation of the Rubik's Cube. Users can interact with the cube, trigger rotations, and observe the concurrent behavior in real-time.

- **Customizable Settings:** The simulator allows users to configure the number of threads used for concurrent rotations and experiment with different synchronization options to observe their impact on performance and correctness.

## Getting Started

To run the Concurrent Rubik's Cube Simulator on your local machine, follow these steps:

1. Clone the repository to your computer:

