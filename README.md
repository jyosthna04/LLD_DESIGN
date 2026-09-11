DesignLoop – LLD Practice & Feedback

Practice LLD. Submit your design. Understand why it works — and how to make it better.

DesignLoop is a web app for practicing Low-Level Design (LLD). Pick a problem, read the requirements, submit your design, and get structured feedback (deterministic checks + AI-generated review) so you can see exactly what to improve — and retry.

🔗 Live demo not deployed yet — see the screenshots/demo below, or run it locally in a few commands (instructions further down).

Features
Browse LLD problems (starts with Parking Lot, Elevator System, Vending Machine)
Read problem requirements and start an attempt
Submit your own class/interface design
Get hybrid feedback:
Deterministic evaluator – objective structural checks
LLM evaluator – subjective feedback (strengths, improvements, score) via Groq API
Review feedback and retry if evaluation fails or AI is unavailable
Attempt lifecycle: STARTED → SUBMITTED → EVALUATING → COMPLETED (or FAILED → Retry)
Tech Stack
Backend: Java, Spring Boot, Spring Web, Spring Data JPA, Hibernate
Database: MySQL
Frontend: HTML, CSS, JavaScript (Thymeleaf templates)
Build: Maven
Testing: JUnit, Spring Boot Test
AI Feedback: Groq API (OpenAI-compatible endpoint, openai/gpt-oss-120b)


Home-Choose a problem
<img width="1920" height="865" alt="image" src="https://github.com/user-attachments/assets/58f6067c-a04b-48d6-9ef4-51973139b410" />
<img width="1920" height="918" alt="image" src="https://github.com/user-attachments/assets/a87827de-2229-4e31-970e-d91419e78250" />
Problems Details
1.Parking Lot
* After clicking view details
* it view the following page
* <img width="1920" height="1026" alt="image" src="https://github.com/user-attachments/assets/b2bd4ce8-1c72-41e4-b3ab-d8ed9de962b5" />
  Then click on start practice
  * after that it is directed to the following page
  * You are allowed to answer the questions
  * <img width="1920" height="1032" alt="image" src="https://github.com/user-attachments/assets/03df6c2c-82d1-4af0-9966-4663b2079ff1" />
  <img width="1920" height="1035" alt="image" src="https://github.com/user-attachments/assets/528b99cb-9dc4-4e55-909b-52de7b788131" /><br>
  * after answering the following questions you can save the draft and then click on feedback
  * you will be directed to next page
  * here in feedback you will be getting your score , strengths, problems ,suggestions, possible trade offs which is give by ai.
  * <img width="1920" height="1030" alt="image" src="https://github.com/user-attachments/assets/b9f78577-d25c-460a-918b-e50f85353a27" />
  <img width="1920" height="1026" alt="image" src="https://github.com/user-attachments/assets/ab9659f2-9051-4e09-8aac-42b7ab1de856" />
  <img width="1920" height="1026" alt="image" src="https://github.com/user-attachments/assets/f4a27c2e-45d9-4ff8-8ea8-5950d461f46a" />


again come back to home page

  2.Elevator system
  * when you click on view deatils on home page
  * it will redirect to the following page
  * <img width="1920" height="965" alt="image" src="https://github.com/user-attachments/assets/584e4008-dd50-4ad2-86fa-2725c95e8dae" />
  * click start practice
  * you will be redirected to the following page

  * <img width="1920" height="974" alt="image" src="https://github.com/user-attachments/assets/416e9b5d-fcae-49df-8cb8-fb255ab2cee9" />
  <img width="1920" height="960" alt="image" src="https://github.com/user-attachments/assets/54a2775a-b851-407a-aae6-ed02d46b5c1d" />


  * click on feedback
  * <img width="1920" height="963" alt="image" src="https://github.com/user-attachments/assets/972253bc-7817-4fd7-a757-0a3201d7ce6a" />
  <img width="1920" height="978" alt="image" src="https://github.com/user-attachments/assets/25b1243e-426d-44f5-90ca-49313131c5ee" />
  <img width="1920" height="970" alt="image" src="https://github.com/user-attachments/assets/df990d3a-8c90-401e-b6a8-f6e9404220a2" />


* come back to home page
* 3.Vending Machine
* when you click on view deatils on home page
*  it will redirect to the following page
*  <img width="1920" height="976" alt="image" src="https://github.com/user-attachments/assets/d8be7318-e450-4fb6-9ca9-06e31e9e150d" />
* click on start practice
* <img width="1920" height="1032" alt="image" src="https://github.com/user-attachments/assets/0dcecab0-56c3-4733-b906-f56a6f220e6b" />
<img width="1920" height="982" alt="image" src="https://github.com/user-attachments/assets/e8746389-e422-4acc-9af2-a9b668b0e3b8" />
click on feedback
<img width="1920" height="961" alt="image" src="https://github.com/user-attachments/assets/33ccb572-2b43-4c72-a6b7-9bab07c351f5" />
<img width="1920" height="973" alt="image" src="https://github.com/user-attachments/assets/576d6abc-d095-4409-9007-f917f47ad8f2" />
<img width="1920" height="968" alt="image" src="https://github.com/user-attachments/assets/06c274db-cda1-4150-a9f1-accf73bbbb4b" />














  


