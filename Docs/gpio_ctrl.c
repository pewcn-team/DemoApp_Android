/*
 *  Copyright 2008-2014 Marvell International Ltd.
 *  All Rights Reserved.
 */

/* ping.c: This file contains the support for network utility ping */

#include <string.h>
#include <wmstdio.h>
#include <wm_os.h>
#include <wm_net.h>
#include <stdlib.h>
#include <mdev_gpio.h>
#include <mdev_pinmux.h>
#include <cli.h>
#include <cli_utils.h>
#include <board.h>
#include "appln_dbg.h"

#define GPIO24_25_26_GPIO PINMUX_FUNCTION_1
#define GPIO_CTRL_DURATION_MSEC  50
#define GPIO_CTRL_IDLE_DURATION_MSEC  500

static mdev_t *pinmux_dev, *gpio_dev;
/* Thread handle */
static os_thread_t gpio_ctrl_thread;
/* buffer to be used as thread stack */
static os_thread_stack_define (thread_stack, 512);
/* save current driving direction */
static char s_current_direction = 0;

static char s_current_state = 0;

/* reset count */
static int s_reset_count = 0;

static void
configure_gpio (int gpio_no)
{
  if (gpio_no > GPIO_7 || gpio_no < GPIO_4) {
    dbg ("gpio is wrong %d", gpio_no);
    return;
  }

  if (gpio_no == GPIO_27) {
    //pinmux_drv_setfunc (pinmux_dev, gpio_no, GPIO27_GPIO27);
  }
  else {
    //pinmux_drv_setfunc (pinmux_dev, gpio_no, GPIO24_25_26_GPIO);
  }
  pinmux_drv_setfunc (pinmux_dev, gpio_no, 0);
  pinmux_drv_close(pinmux_dev);

  gpio_drv_setdir (gpio_dev, gpio_no, GPIO_OUTPUT);
  /* Off LEDs */
  gpio_drv_write (gpio_dev, gpio_no, 0);
  //gpio_drv_close(gpio_dev);

//os_thread_sleep (os_msec_to_ticks (GPIO_CTRL_DURATION_MSEC));

    //gpio_drv_write (gpio_dev, GPIO_4, GPIO_IO_LOW);
}

static void
init_gpio ()
{
  int gpio_index = 0;
  /* Initialize  pinmux driver */
  pinmux_drv_init ();

  /* Open pinmux driver */
  pinmux_dev = pinmux_drv_open ("MDEV_PINMUX");

  /* Initialize GPIO driver */
  int result = gpio_drv_init ();
  dbg ("%d gpio_drv_init result", result);
  /* Open GPIO driver */
  gpio_dev = gpio_drv_open ("MDEV_GPIO");

  for (gpio_index = GPIO_4; gpio_index <= GPIO_7; gpio_index++) {
    configure_gpio (gpio_index);
  }
  //gpio_drv_setdir (gpio_dev, GPIO_4, GPIO_INPUT);
  //gpio_drv_write (gpio_dev, GPIO_6, GPIO_IO_LOW);
}

static void
deinit_gpio ()
{
  pinmux_drv_close (pinmux_dev);

  gpio_drv_open ("MDEV_GPIO");
}

static void
display_car_ctrl_usage ()
{
  wmprintf ("Usage:\r\n");
  wmprintf ("\tcar [u|d|l|r]\r\n");
}

static void
gpio_car_ctrl_thread_routine (os_thread_arg_t data)
{

  /* loop forever */
  while (1) {
    while (s_current_direction) {
      //dbg ("%c is current direction", s_current_direction + '0');
      //GPIO_WritePinOutput (s_current_direction, GPIO_IO_LOW);
      if (s_current_state==0)
      {
        gpio_drv_write (gpio_dev, s_current_direction, GPIO_IO_LOW);
      }
      else
      {
        gpio_drv_write (gpio_dev, s_current_direction, GPIO_IO_HIGH);
      }
      
      os_thread_sleep (os_msec_to_ticks (GPIO_CTRL_DURATION_MSEC));
      //GPIO_WritePinOutput (s_current_direction, GPIO_IO_HIGH);
      //os_thread_sleep (os_msec_to_ticks (GPIO_CTRL_DURATION_MSEC));

      s_reset_count = (s_reset_count + 1) % 5;
      if (!s_reset_count) {
	/* 
	 * due to no timer system, you know, we do NOT initialize it,
	 * we have to use counter mechanism to let car stop.
	 */
	//dbg ("car should stop now");
	//s_current_direction = 0;
      }

    }

    os_thread_sleep (os_msec_to_ticks (GPIO_CTRL_IDLE_DURATION_MSEC));
    //dbg ("gpio ctrl thread is in idle state");
  }
}

static void
gpio_car_ctrl (int gpio_no, int state)
{
  /* remember current driving direction */
  s_current_direction = (char) gpio_no;
  s_current_state = state;
  /* reset count */
  s_reset_count = 0;  
}

int
gpio_car_dir_ctrl (char dir)
{
  if (dir == 'u') {
    dbg ("%s up gpio24", __func__);
    gpio_car_ctrl (GPIO_4, 1);
  }
  else if (dir == 'd') {
    dbg ("%s down gpio25", __func__);
    gpio_car_ctrl (GPIO_5, 1);
  }
  else if (dir == 'l') {
    dbg ("%s down gpio26", __func__);
    gpio_car_ctrl (GPIO_6, 1);
  }
  else if (dir == 'r') {
    dbg ("%s down gpio27", __func__);
    gpio_car_ctrl (GPIO_7, 1);
  }
    if (dir == 'i') {
    dbg ("%s up gpio24", __func__);
    gpio_car_ctrl (GPIO_4, 0);
  }
  else if (dir == 'f') {
    dbg ("%s down gpio25", __func__);
    gpio_car_ctrl (GPIO_5, 0);
  }
  else if (dir == ';') {
    dbg ("%s down gpio26", __func__);
    gpio_car_ctrl (GPIO_6, 0);
  }
  else if (dir == 't') {
    dbg ("%s down gpio27", __func__);
    gpio_car_ctrl (GPIO_7, 0);
  }

  return WM_SUCCESS;
}

void
cmd_car_ctrl (int argc, char **argv)
{
  char dir;
  if (argc != 2) {
    goto end;
  }
  dir = argv[1][0];
  if (gpio_car_dir_ctrl (dir) != WM_SUCCESS) {
    goto end;
  }

  return;
end:
  wmprintf ("Incorrect usage\r\n");
  display_car_ctrl_usage ();
}

static struct cli_command car_ctrl_clis[] = {
  {
   "car", "[u|d|l|r] u-up d-down l-left r-right", cmd_car_ctrl},
};

int
car_cli_init (void)
{
  int i;
  for (i = 0; i < sizeof (car_ctrl_clis) / sizeof (struct cli_command); i++)
    if (cli_register_command (&car_ctrl_clis[i]))
      return -WM_FAIL;
  init_gpio ();
  os_thread_create (&gpio_ctrl_thread,	/* thread handle */
		    "gpio_ctrl_thread",	/* thread name */
		    gpio_car_ctrl_thread_routine,	/* thread routine */
		    0,		/* thread argument, ignored in this case */
		    &thread_stack,	/* stack */
		    OS_PRIO_2);	/* priority - medium low */
  return WM_SUCCESS;
}

int
car_cli_deinit (void)
{
  int i;
  for (i = 0; i < sizeof (car_ctrl_clis) / sizeof (struct cli_command); i++)
    if (cli_unregister_command (&car_ctrl_clis[i]))
      return -WM_FAIL;
  deinit_gpio ();
  return WM_SUCCESS;
}
