
#ifndef RADIOPROTOCOLMESSAGE_H
#define RADIOPROTOCOLMESSAGE_H

typedef nx_struct RadioMsg_ {
  nx_uint16_t counter;
  nx_uint16_t node_id; // Origem da mensagem
  nx_bool base_station; // Vem da estacao de base
  nx_bool buzzer_enabled;	// Buzzer activado?
  nx_bool motion_enabled; 	// Detecao de movimento activada?
  nx_bool propagation_effect_enabled;
  nx_bool buzzer_off;
  nx_bool motion_detected;
  nx_bool tone_detected;
  nx_bool shutdown;


} RadioMsg;

enum {
  AM_RADIOPROTOCOLMESSAGE = 6
};


#endif
