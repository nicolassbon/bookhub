# BookHub Legacy — Feature Inventory

## Purpose

This document summarizes the features detected in the `legacy/spring-mvc` branch and serves as a baseline for the migration to a modern architecture.

## Executive summary

The legacy version is a Spring MVC + Thymeleaf monolith focused on a social reading platform. It currently covers authentication, profiles, personal library management, reviews, social features, gamification, subscriptions/payments, and in-app notifications.

## 1. Authentication and accounts

- User registration.
- Session-based login and logout.
- Password recovery via email verification code.
- Initial onboarding flow after signup.

## 2. Profiles and users

- Own and third-party profile pages.
- Personal data editing.
- User search.
- Profile image upload to the local filesystem.

## 3. Personal library and reading

- Search books by title/author.
- Book detail page.
- Reading states:
  - Want to Read
  - Reading
  - Read
- Read pages tracking.
- Reading progress calculation.
- Shelf views grouped by state.
- Yearly reading statistics.

## 4. Reviews and comments

- Create or edit a personal review for a book.
- Explore reviews.
- Filter by book, review author, and book author.
- Sort by score.
- Like/dislike reactions.
- Comments on reviews.
- Delete comment by author.

## 5. Social features

- Friend requests.
- Accept, reject, or remove friendships.
- Friends list.
- Social feed with posts between friends.
- Comments on posts.
- Social reading activity.

## 6. Goals, challenges, and achievements

- Yearly reading goal.
- Personal and community challenge view.
- Predefined achievements.
- Custom achievements.
- Achievement states: in progress / completed / not completed.

## 7. Subscriptions and payments

- Bronze, Silver, and Gold plans.
- Feature restrictions by plan.
- Upgrade/downgrade plan flow.
- MercadoPago payment flow.
- Monthly expiration and automatic downgrade.

## 8. In-app notifications

- Typed notifications.
- User-specific assignment.
- Used for friendship and achievement events.
- Rendered on the home page with contextual actions.

## 9. Recommendations

- Heuristic recommendation based on favorite genre.
- Random book fallback.

## Main user flows detected

1. Sign up → onboarding → home.
2. Search book → open detail → add to library → update status/progress.
3. Create review → explore reviews → react/comment.
4. Search user → send friend request → accept/reject from notification.
5. Browse plans → pay for upgrade → update subscription plan.
6. Define yearly goal → track progress → unlock achievements.

## Admin functionality detected

Support for the `Admin` role exists, but no clear administration or moderation module was found through controllers or views. This must be manually verified before defining feature parity.

## External integrations detected

- MercadoPago.
- SMTP/Email for password recovery.
- MySQL as the main database.
- HSQLDB in test/dev environments.

## Gaps and open questions to validate

- No clear admin panel was found.
- No explicit server-to-server MercadoPago webhook was found, only browser callbacks.
- The model supports read/unread notifications, but the full flow is unclear.
- The current recommendation logic is basic, not an advanced engine.
- There is technical debt around hardcoded dates and embedded credentials.

## Recommended use of this document

- Define what belongs in V1.
- Identify what moves to V2/V3.
- Use it as a functional parity checklist against the legacy version.
