
# Explorator

## Table of Contents
1. [Overview](#Overview)
1. [Product Spec](#Product-Spec)
1. [Wireframes](#Wireframes)
2. [Schema](#Schema)

## Overview
### Description
Users can swipe through restaurants in an area, adding ones they like to their list and enabling them to check restaurants off after visiting.

### App Evaluation
- **Category:** Attractions/Dining/Tourism
- **Mobile:** The swipe feature makes this app easier on mobile, but could also be developed into a website using other gestures. The mobile version could also include more features like contact syncing to see contacts' lists and connecting to navigation.
- **Story:** Users can create an account and choose a location, then view restaurants and choose 'not interested' or add to their list. Users can view their profile with their list and mark items as complete.
- **Market:** Anyone who likes to eat out could use this app, and it would especially appeal to people who want to try new things, are going on vacation, or moving to a new area.
- **Habit:** This app is designed to be used whenever someone is making a restaurant decision. Adding notifications could push more regular use.
- **Scope:** The simple version of the app is described above. This could be enhanced by allowing users to connect with their friends and view their list, allowing more ways to filter swiping and searching through list, and getting more details about a restaurant.

## Product Spec

### 1. User Stories (Required and Optional)

**Required Must-have Stories**

* User can create a new account
* User can login to an existing account
* User can choose a location
* User can swipe through a list of a restaurants and add to list
* User can view their list
* User can check items off their list
* User can add a profile image

**Optional Nice-to-have Stories**

* User can choose other search filters for swipe list
* User can remove items from their list
* User can get more details about a restaurant while swiping or in list
* User can follow friends and view their list
* User can reset and add back restaurants they already chose 'not interested' on
* User can choose a neutral option and the restaurant is added back to the cycle
* User can choose other types of attractions to explore, not just restaurants

### 2. Screen Archetypes

* Login
   * User can login to an existing account
* Create Account
   * User can create a new account
* Swipe (home)
    * User can choose a location
    * User can swipe through a list of a restaurants and add to list
* Profile
    * User can view their list
    * User can check items off their list
    * User can add a profile image

### 3. Navigation

**Tab Navigation** (Tab to Screen)

* Swipe
* Profile

**Flow Navigation** (Screen to Screen)

* Login
   * Create Account
   * Swipe (home)
* Create Account
   * Swipe (home)
* Swipe (home)
    * (Optional) Restaurant details
* Profile
    * (Optional) Restaurant details

## Wireframes
<img src="https://github.com/kateluckerman/Explorator/blob/master/wireframes.jpg" width=600>

## Schema 
### Models
#### User

   | Property      | Type     | Description |
   | ------------- | -------- | ------------|
   | objectId      | String   | unique id for the user (default field) |
   | username       | String | unique username for user (default field) |
   | password     | String   | password for user (default field) |
   | profileImage     | File   | profile picture for user |
   | name | String | name of user |
   |list | Array[Business] | list of user's saved businesses |
   
#### Business

   | Property      | Type     | Description |
   | ------------- | -------- | ------------|
   | objectId      | String   | unique id for the business (default field) |
   | name      | String | name of business |
   | categories     | Array[String]  | list of business's categories |
   | location     | String   | general location of business |
   | address | String | address of business |
   |price | Number | 1-5 integer rank of price level |
   |rating | Number | 1-5 rank of user ratings |
   |image | File | primary image for attraction |
   
### Networking
#### List of network requests by screen
   - Swipe/Home Screen
      - (Read/GET) List of businesses from Yelp API
      - (Create/POST) Create a new business object when restaurant is saved
   - Profile Screen
      - (Read/GET) Query logged in user object
      - (Update/PUT) Update user profile image
      - (Update/PUT) Update user's name
      - (Read/GET) Query user's saved restaurants
     
#### Existing API Endpoints
##### Yelp Fusion API
- Base URL - [https://api.yelp.com/v3](https://api.yelp.com/v3)

   HTTP Verb | Endpoint | Description
   ----------|----------|------------
    `GET`    | /businesses/search | get list of businesses based on location
