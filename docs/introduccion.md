# Introduction

## Context

**Astro Merge** is a **sports tournament** management platform built with a
microservices architecture. Within that platform, the **Communications Service**
(`am-comunication-service`) concentrates all messaging between the actors of the system:
players, organizers, and the support system.

The service combines a **WebSocket (STOMP)** channel for real-time client↔server
interaction with a **REST API** for management operations (history, moderation) and
for communication with other microservices. Persistence is done in **PostgreSQL**.

## Problem

On a tournament platform, communication happens in multiple contexts —support,
direct messaging between players, team group chats— and must be immediate and reliable.
Solving this within each service would create coupling and duplication. A **specialized**
microservice is needed that:

- Offers **real-time** messaging without resorting to *polling*.
- Manages different **conversation types** (support, direct, group) in a uniform way.
- Allows **escalating** support conversations to the organizer.
- Provides **moderation** of reported content.
- Maintains **integrity** between conversations, messages, and reports.
- Integrates with the rest of the platform (teams, identity, notifications, auditing).
