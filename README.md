## POC_V1 – Proof of Concept for Wear OS ↔ Android Messaging
![Static Badge](https://img.shields.io/badge/Wearable%20Data%20Layer-POC-blue)

This project is a combined demonstration of bidirectional communication between a Wear OS app and an Android phone app using the Wearable Data Layer API.

It integrates the core logic of message sending and receiving on both ends and acts as a working prototype to ensure the communication layer is fully functional.

You can check this demo video for more context-

https://github.com/user-attachments/assets/8ee06aa3-e67a-4f3a-bf9f-51dde1cd332e


## What's Inside?
This repo includes both:
- A Wear OS module (Kotlin + Jetpack Compose)
- An Android mobile module (Kotlin + Jetpack Compose)

These apps demonstrate sending and receiving messages across devices using the **MessageClient API**.


## Technologies Used
- Kotlin
- Jetpack Compose
- Wearable Data Layer API
  - MessageClient – for real-time message communication.


## Permissions Used
```
<uses-permission android:name="android.permission.WAKE_LOCK" />
```



## Local Setup Options

### Option 1: Use this combined repo (POC_V1)
This repo contains both the Wear OS and Android modules bundled together. While it's suitable for testing the full functionality in one place, running it could be a little bit difficult, if you have never done this before [just like me :)]. 


#### Steps to Run the Combined Repo:

Just clone this repo and open it on Android Studio and run both the apps (there is a dropdown button right next to the run button inside the Android Studio; select one by one with the right devices selected)

1. Clone the repo:
    ```
    git clone https://github.com/Abhishek14104/POC_V1.git
    ```
2. Open it in Android Studio.
3. Run both the Wear OS module and the Mobile app module simultaneously in Android Studio.

   You will need to run both the **wear folder** and **app folder** at the same time to test the messaging functionality between the two devices.


### Option 2: Use the individual repos for easier setup
For a simpler setup, I recommend using whatever suits your ease.

#### You can use these separate repos:
- [Wear Companion](https://github.com/Abhishek14104/wear_companion) – The Wear OS app module.
- [Mobile Companion](https://github.com/Abhishek14104/mobile_companion) – The Android mobile app module.
