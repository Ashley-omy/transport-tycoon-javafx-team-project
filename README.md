# 🚚🚗Transport Tycoon JavaFX Team Project 🛣🌳💶

_Please find the Japanese version below._

A team-based Java game project inspired by Transport Tycoon, developed as part of a university software development course.

## Overview

This project is a transport simulation game where players build and manage transportation infrastructure. The application is designed based on the MVC architecture to separate game logic, user interaction, and visual rendering.

This game has been built using GitLab.

## How to Play

### Important Rules

- You are the boss of a transport company and are given an initial budget. You can use it to construct transport routes and purchase vehicles.
- You make money when vehicles successfully unload materials, products, or passengers at their destinations.
- Maintenance fees apply and increase as vehicles age. If a vehicle gets too old, you can sell it.
- When you run out of budget (coins), your company goes bankrupt and the game is over.
- Forests expand their area over time. Placing roads over them costs extra money.

### 1. Download the game

To download the game, go to "Release" and click the jar file that corresponds to your computer's OS.

### 2. Make a route of transport

Make sure to construct roads so that they properly connect factories, mines, and cities.
Factories consume materials from mines, and cities then buy products from the factories. If you connect two cities and run a bus on the road between them, passengers will generate profit.

Place pieces of road first, then place stops, where vehicles load and unload goods or people. After that, place a garage, where you will be able to buy vehicles later. To create a route, click the "Place Route" button on the side bar, then click the stops that belong to the route one by one. Click "Place Route" again once you are done.

![Making a route](https://github.com/user-attachments/assets/e0000a27-6d95-45ea-813f-f5de5b06def6)

### 3. Purchase vehicles

Click the garage and choose a vehicle. There are two types: buses and trucks. Buses carry passengers, while trucks carry materials and products. Price, speed, capacity, and maintenance fee vary depending on the vehicle, so choose carefully.

![Purchasing vehicles](https://github.com/user-attachments/assets/47ffd767-5d70-4728-be83-f31829d98a53)

## Technologies

- Java
- JavaFX
- Maven
- JUnit
- GitLab
- CI/CD pipelines

## My Contributions

- Designed UML diagrams to clarify the system architecture
- Contributed to the JavaFX-based UI implementation
- Implemented vehicle movement logic
- Worked in a team development environment using GitLab

## Architecture

The application follows the MVC pattern:

- Model: game world, entities, transport logic, and simulation state
- View: JavaFX UI and map rendering
- Controller: user input handling and coordination between Model and View

## Screenshots and Design Documents

### 1. UML Class Diagrams

These UML diagrams were designed to clearly visualize class relationships and method responsibilities at a glance. Following the MVC architecture, they were used to support smoother team collaboration and system design.

Package
![Package Diagram](images/package_UML.png)

Model
![Model Diagram](images/model_UML.png)

View
![View Diagram](images/view_UML.png)

Controller
![Controller Diagram](images/controller_UML.png)

### 2. CI/CD Pipeline

Used GitLab CI pipelines to support automated testing and collaborative development.

![CI Pipeline](images/pipeline.png)

### 3. GitLab Team Development

These screenshots demonstrate the collaborative development workflow using GitLab, including branch management, issue tracking, task organization.

![GitLab IssueBoard](images/issue_board.png)
![GitLab RepositoryGraph](images/repository_graph.png)

---

大学のソフトウェア開発の授業の一環として制作した、Transport Tycoonにインスパイアされたチーム制作のJavaゲームプロジェクトです。

## 概要

このプロジェクトは、プレイヤーが交通インフラを建設・運営する輸送シミュレーションゲームです。ゲームロジック、ユーザー操作、画面描画を分離するため、MVCアーキテクチャに基づいて設計されています。GitLabを使って開発されました。

## 遊び方

### 基本ルール

- あなたは輸送会社の社長であり、初期予算が与えられます。この予算を使って輸送ルートを建設したり、車両を購入したりできます。
- 車両が目的地で資材、製品、乗客の荷下ろしをすると収入が得られます。
- 維持費が発生し、車両が古くなるほど増加します。車両が古くなりすぎた場合は売却できます。
- 予算(コイン)がなくなると、会社は倒産しゲームオーバーになります。
- 森は時間の経過とともに面積が広がります。森の上に道路を敷設すると追加費用がかかります。

### 1. ゲームのダウンロード

ゲームをダウンロードするには、「Release」に移動し、お使いのパソコンのOSに対応したjarファイルをクリックしてください。

### 2. 輸送ルートの作成

道路が工場、鉱山、都市を適切に結ぶように建設してください。
工場は鉱山から資材を消費し、都市は工場から製品を購入します。2つの都市を結び、その道路にバスを走らせると、乗客が利益を生みます。

まず道路を敷設し、次に車両が荷物や人を積み下ろしする停留所を設置します。その後、後で車両を購入できる車庫を設置します。ルートを作成するには、サイドバーの「Place Route」ボタンをクリックし、続けてルートに含める停留所を順番にクリックしていきます。完了したら、もう一度「Place Route」をクリックしてください。

![Making a route](https://github.com/user-attachments/assets/e0000a27-6d95-45ea-813f-f5de5b06def6)

### 3. 車両の購入

車庫をクリックして車両を選択します。車両にはバスとトラックの2種類があります。バスは乗客を、トラックは資材や製品を輸送します。価格・速度・積載量・維持費は車両によって異なるため、よく検討して選んでください。

![Purchasing vehicles](https://github.com/user-attachments/assets/47ffd767-5d70-4728-be83-f31829d98a53)

## 使用技術

- Java
- JavaFX
- Maven
- JUnit
- GitLab
- CI/CDパイプライン

## 担当箇所

- UML図の設計
- JavaFXベースのUI実装
- 車両移動ロジックの実装
- GitLabを用いたチーム開発環境での作業

## アーキテクチャ

このアプリケーションはMVCパターンに従っています:

- Model: ゲーム世界、工場や街などのエンティティ、輸送ロジック、ゲームの状態
- View: JavaFXのUIとマップ描画
- Controller: ユーザー入力の処理とModel・View間の連携

## スクリーンショットと設計資料

### 1. UMLクラス図

これらのUML図は、クラス間の関係とメソッドの役割を一目で分かりやすく可視化するために設計されました。MVCアーキテクチャに沿って、チーム開発をスムーズに進めることに大きく貢献しました。

Package
![Package Diagram](images/package_UML.png)

Model
![Model Diagram](images/model_UML.png)

View
![View Diagram](images/view_UML.png)

Controller
![Controller Diagram](images/controller_UML.png)

### 2. CI/CDパイプライン

自動テストとチーム開発促進のため、GitLabのCIパイプラインを使用しました。

![CI Pipeline](images/pipeline.png)

### 3. GitLabでのチーム開発

これらのスクリーンショットは、ブランチ管理・イシュー管理・タスク管理を含む、GitLabを用いたチーム開発のワークフローを示しています。

![GitLab IssueBoard](images/issue_board.png)
![GitLab RepositoryGraph](images/repository_graph.png)
