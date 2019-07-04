

void init_robot() {
  Serial.begin(115200);

  init_motor_pwm(0);

  pinMode(Motor_1_A, OUTPUT);
  pinMode(Motor_1_B, OUTPUT);

  pinMode(Motor_2_A, OUTPUT);
  pinMode(Motor_2_B, OUTPUT);

  pinMode(Motor_3_A, OUTPUT);
  pinMode(Motor_3_B, OUTPUT);

  pinMode(Motor_4_A, OUTPUT);
  pinMode(Motor_4_B, OUTPUT);


}
