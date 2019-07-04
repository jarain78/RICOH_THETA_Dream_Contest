#include "Config.h"

void setup() {
  init_robot();

}


void loop() {
  String data_to_control_robot = read_data_from_camera();
  robot_control_mode_one( data_to_control_robot); 


}
