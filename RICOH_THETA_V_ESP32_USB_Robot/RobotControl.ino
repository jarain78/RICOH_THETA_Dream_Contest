

// ------------------------------------------
// Mode one of Navegation
// ------------------------------------------

void robot_control_mode_one(String data_to_control_robot) {
  if (data_to_control_robot.equals("adelante")) {
    Serial.println("Adelante");
    move_forward();
  } else if (data_to_control_robot.equals("atras")) {
    Serial.println("Atras");
    move_backward();
  } else if (data_to_control_robot.equals("izquierda")) {
    Serial.println("Izquierda");
    move_left();
  } else if (data_to_control_robot.equals("derecha")) {
    Serial.println("Derecha");
    move_right();
  } else if (data_to_control_robot.equals("ccw")) {
    Serial.println("CCW");
    rotate_ccw();
  } else if (data_to_control_robot.equals("cccw")) {
    Serial.println("CCCW");
    rotate_cccw();
  } else if (data_to_control_robot.equals("test")) {
    Serial.println("TEST");
    test();
  } else {
    //Serial.println("Stop");
    stop_robot();
  }
}

void init_motor_pwm(int ch) {
  ledcSetup(ch, freq, resolution);

  ledcAttachPin(pwm_pin_m_1, ch);
  ledcAttachPin(pwm_pin_m_2, ch);
  ledcAttachPin(pwm_pin_m_3, ch);
  ledcAttachPin(pwm_pin_m_4, ch);

}

void test() {
  digitalWrite(Motor_2_A, HIGH);
  digitalWrite(Motor_2_B, LOW);

  /*digitalWrite(Motor_3_A, HIGH);
    digitalWrite(Motor_3_B, LOW);

    digitalWrite(Motor_2_A, HIGH);
    digitalWrite(Motor_2_B, LOW);

    digitalWrite(Motor_4_A, HIGH);
    digitalWrite(Motor_4_B, LOW);*/

  /*ledcWrite(pwm_pin_m_1, dutyCycle);
    delay(15);
    ledcWrite(pwm_pin_m_2, dutyCycle);
    delay(15);
    ledcWrite(pwm_pin_m_3, dutyCycle);
    delay(15);*/
  ledcWrite(channel_pwm, dutyCycle);

}



void move_forward() {

  digitalWrite(Motor_1_A, HIGH);
  digitalWrite(Motor_1_B, LOW);

  digitalWrite(Motor_2_A, LOW);
  digitalWrite(Motor_2_B, LOW);

  digitalWrite(Motor_3_A, LOW);
  digitalWrite(Motor_3_B, HIGH);

  digitalWrite(Motor_4_A, LOW);
  digitalWrite(Motor_4_B, LOW);

  ledcWrite(channel_pwm, dutyCycle);

}

void move_backward() {

  digitalWrite(Motor_1_A, LOW);
  digitalWrite(Motor_1_B, HIGH);

  digitalWrite(Motor_2_A, LOW);
  digitalWrite(Motor_2_B, LOW);

  digitalWrite(Motor_3_A, HIGH);
  digitalWrite(Motor_3_B, LOW);

  digitalWrite(Motor_4_A, LOW);
  digitalWrite(Motor_4_B, LOW);


  ledcWrite(channel_pwm, dutyCycle);

}

void move_right() {

  digitalWrite(Motor_1_A, LOW);
  digitalWrite(Motor_1_B, LOW);

  digitalWrite(Motor_2_A, LOW);
  digitalWrite(Motor_2_B, HIGH);

  digitalWrite(Motor_3_A, LOW);
  digitalWrite(Motor_3_B, LOW);

  digitalWrite(Motor_4_A, HIGH);
  digitalWrite(Motor_4_B, LOW);

  ledcWrite(channel_pwm, dutyCycle);

}

void move_left() {

  digitalWrite(Motor_1_A, LOW);
  digitalWrite(Motor_1_B, LOW);

  digitalWrite(Motor_2_A, HIGH);
  digitalWrite(Motor_2_B, LOW);

  digitalWrite(Motor_3_A, LOW);
  digitalWrite(Motor_3_B, LOW);

  digitalWrite(Motor_4_A, LOW);
  digitalWrite(Motor_4_B, HIGH);

  ledcWrite(channel_pwm, dutyCycle);

}

void move_diagonal_line() {

}

void rotate_ccw() {

  digitalWrite(Motor_3_A, LOW);
  digitalWrite(Motor_3_B, HIGH);

  digitalWrite(Motor_1_A, HIGH);
  digitalWrite(Motor_1_B, LOW);

  digitalWrite(Motor_4_A, LOW);
  digitalWrite(Motor_4_B, HIGH);

  digitalWrite(Motor_2_A, HIGH);
  digitalWrite(Motor_2_B, LOW);

  ledcWrite(channel_pwm, dutyCycle);

}


void rotate_cccw() {

  digitalWrite(Motor_3_A, HIGH);
  digitalWrite(Motor_3_B, LOW);

  digitalWrite(Motor_1_A, LOW);
  digitalWrite(Motor_1_B, HIGH);

  digitalWrite(Motor_4_A, HIGH);
  digitalWrite(Motor_4_B, LOW);

  digitalWrite(Motor_2_A, LOW);
  digitalWrite(Motor_2_B, HIGH);

  ledcWrite(channel_pwm, dutyCycle);

}

void stop_robot() {
  digitalWrite(Motor_1_A, LOW);
  digitalWrite(Motor_1_B, LOW);

  digitalWrite(Motor_2_A, LOW);
  digitalWrite(Motor_2_B, LOW);

  digitalWrite(Motor_3_A, LOW);
  digitalWrite(Motor_3_B, LOW);

  digitalWrite(Motor_4_A, LOW);
  digitalWrite(Motor_4_B, LOW);

  ledcWrite(channel_pwm, 0);
}


// ------------------------------------------
// Mode two of Navegation
// ------------------------------------------

void move_forward_mode_two() {
  digitalWrite(Motor_1_A, LOW);
  digitalWrite(Motor_1_B, HIGH);

  digitalWrite(Motor_3_A, LOW);
  digitalWrite(Motor_3_B, HIGH);

  digitalWrite(Motor_2_A, HIGH);
  digitalWrite(Motor_2_B, LOW);

  digitalWrite(Motor_4_A, HIGH);
  digitalWrite(Motor_4_B, LOW);

  ledcWrite(channel_pwm, dutyCycle);

}
