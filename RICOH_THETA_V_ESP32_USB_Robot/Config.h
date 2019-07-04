
/*
   PWM Pins OK
   channel_pwm = 0
   Motor 1
   pwm_pin = 12 --> verde
   io_pin1 = 13 --> negro
   io_pin2 = 17 --> gris

   channel_pwm = 0
   Motor 2
   pwm_pin = 14 --> verde
   io_pin1 = 27 --> amarillo
   io_pin2 = 26 --> gris

   channel_pwm = 0
   Motor 3
   pwm_pin = 25 --> blanco
   io_pin1 = 33 --> amarillo
   io_pin2 = 32 --> cafe

   channel_pwm = 0
   Motor 4   
   pwm_pin = 5 --> azul
   io_pin1 = 18 --> naranja
   io_pin2 = 23 --> blanco
*/


int Motor_1_A = 13;
int Motor_1_B = 17;

int Motor_2_A = 21; 
int Motor_2_B = 27; 

int Motor_4_A = 33;  // ok
int Motor_4_B = 32;

int Motor_3_A = 18;
int Motor_3_B = 23;

int pwm_pin_m_1 = 12;  //
int pwm_pin_m_2 = 14;  //
int pwm_pin_m_3 = 25;  // ok
int pwm_pin_m_4 = 5;  //

String serial_data = " ";

// setting PWM properties
const int freq = 5000;
const int resolution = 8;
const int dutyCycle = 200;
int channel_pwm = 0;
