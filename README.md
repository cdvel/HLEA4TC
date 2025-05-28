# HLEA4TC - Hierarchical Learning Engine for Adaptive Traffic Control

A JADE-based multi-agent framework designed for distributed traffic control optimization with support for hierarchical agent organization and external traffic simulation integration.

## Overview

HLEA4TC provides a foundation for implementing hierarchical reinforcement learning algorithms in traffic control systems. The framework establishes a two-tier agent hierarchy where Sector agents manage groups of Junction agents, enabling coordinated traffic optimization across urban networks.

## Architecture

### Agent Types

- **Junction Agents**: Represent individual traffic intersections
  - Subscribe to sector agents for coordination
  - Receive state updates from external traffic simulation
  - Maintain local traffic state information (coordination, priority, traffic counts)

- **Sector Agents**: Manage groups of junction agents
  - Handle junction subscription requests
  - Negotiate with other sectors for junction assignments
  - Coordinate junction behaviors within their sector

### Key Components

- **PlatformMediator**: Central management interface for the JADE platform
  - Manages agent lifecycle (creation, initialization, updates)
  - Bridges between external systems and the agent platform

- **ControllerNativeInterface**: JNI bridge for simulation integration
  - Connects to external traffic simulation (e.g., PARAMICS)
  - Enables real-time state updates from simulation environment

### Communication Protocols

The system implements FIPA-compliant protocols:
- **Subscribe/Inform**: Junctions subscribe to sectors for coordination
- **Propose**: Sectors negotiate junction assignments with each other

## Features

- **Dynamic Agent Discovery**: Agents discover each other via JADE's Directory Facilitator
- **Hierarchical Organization**: Two-tier structure for scalable traffic management
- **External Integration**: JNI interface for connecting to traffic simulators
- **Asynchronous Updates**: Object-to-Agent (O2A) communication for real-time state updates
- **Distributed Negotiation**: Sectors autonomously negotiate junction assignments

## Current Implementation Status

The framework currently provides:
- ✅ Multi-agent communication infrastructure
- ✅ Hierarchical agent organization
- ✅ External simulation integration via JNI
- ✅ Dynamic agent management
- ✅ Basic negotiation protocols

Not yet implemented:
- ❌ Reinforcement learning algorithms
- ❌ Q-learning or policy gradient methods
- ❌ Reward/cost functions for optimization
- ❌ State-action space definitions
- ❌ Learning rate and exploration strategies

## Usage

### Starting the Platform

```java
// Initialize platform with junction IDs
PlatformMediator.startJadePlatform("101 102 103 104");

// Add sector agents
PlatformMediator.initSectorAgent("S-001");
PlatformMediator.initSectorAgent("S-002");
```

### Updating Junction States

```java
// Update junction with traffic data
JunctionUpdateBean update = new JunctionUpdateBean(junctionID);
update.incomingCounts = Arrays.asList(10, 15, 8, 12); // N,S,E,W
update.priority = 45;
update.coordination = 0; // E-W coordination
PlatformMediator.updJunctionAgent("101");
```

## Dependencies

- JADE 4.3.1 or higher
- Java 1.6 or higher
- Native traffic simulation library (via JNI)

## Future Development

This framework is designed to support hierarchical reinforcement learning algorithms such as:
- Hierarchical Q-learning for sector-level coordination
- Multi-agent actor-critic methods for junction control
- Transfer learning between similar traffic patterns
- Coordinated exploration strategies for system-wide optimization

## License

[License information to be added]

## Contributing

[Contribution guidelines to be added]
