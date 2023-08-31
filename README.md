# CZ3004-MDP-Robot-Android-UI
Android interface for giving instructions and environment input to maze-solving robot

To run:
1. Download Android Studios: https://developer.android.com/studio
2. Build with embedded JDK
3. Download emulator with supported API(Hardware used was Samsung Galaxy S6)
4. Run the code (Bluetooth portion only works when connected with RPI or external app emulator) 

Technologies: Java and Android ecosystem

## Main Idea
1. Provide UI for visualization of robot and instruction giving.
2. Connects to Raspberry Pi with bluetooth socket.
3. Raspberry Pi talks to Algorithm and Robot module

## Purpose
Provide UI and interface for robot path-finding tasks.

# Features
## Android frontend (UI/UX) (100% code contribution)
1. Zombie themed UX
2. Interactable and draggable car and arena objects
3. Queue system to accept car movement simulation
4. Able to accept instructions to place car and objects on certain points on grid map and with direction
5. Able to save and load presets

## Android bluetooth (25% code contribution)
1. Connects to Raspberry Pi through bluetooth socket
2. Accepts and sends bit instructions that python and c++ server understand
   
# Key challenges/considerations and solution

| Challenge        | Considerations           | Solution  |
| ------------- |:-------------:| -----:|
| Reconciling actual robot movement and movement in instructions and android ui|Robot, android ui and algorithm simulation need to match.  | Agree on fixed grid distannce for car to move in 6 directions. |
| Agree on transmission language | All listeners need to understand the instructions, fitting the connection bandwidth and encryption etc. | Have Raspberry Pi assign our communication approach. Centralising control. |

# Biggest Takeaways
1. Learning more about the AWS ecosystem especially AWS lambda, how to host backend and frontend apps, architect the cloud applications and see the finOPS angle when it comes to costs.
2. Buying and owning a domain, configuring DNS and setting up SSL.
3. Managing user sessions with JWT tokens, using IAM roles for resource access management and API key management.
4. Prompt engineering and exploring the methods by which AI can improve or complement learning.
