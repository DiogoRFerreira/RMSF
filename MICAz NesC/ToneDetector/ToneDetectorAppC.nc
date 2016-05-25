

configuration ToneDetectorAppC {}

implementation
{
  components MainC, LedsC;
  components ToneDetectorC as App;
  components MicDeviceP as MicrophoneC;
	
  components ActiveMessageC;
  components new AMSenderC(AM_RADIOPROTOCOLMESSAGE);
  components new AMReceiverC(AM_RADIOPROTOCOLMESSAGE);
    components SounderC;

//-------------------------------------------------------
  App.Boot -> MainC;

 // BlinkC.Timer0 -> Timer0;
  App.Leds -> LedsC;
  App.MicSetting -> MicrophoneC.MicSetting;
  App.MicResource -> MicrophoneC.Resource[1];
	
  App.Packet -> AMSenderC;
  App.AMPacket -> AMSenderC;
  App.AMControl -> ActiveMessageC;
  App.AMSend -> AMSenderC;
    App.Receive -> AMReceiverC;
      App.Sounder -> SounderC.Mts300Sounder;
}

