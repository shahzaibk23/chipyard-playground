#include <stdio.h>
#include "mmio.h"

#define TOBEREAD 0x2000


unsigned int justRead_ref(){
    return 230;
}

int main(void){
    uint32_t    result,
                ref;
    
    result = reg_read32(TOBEREAD);
    ref = justRead_ref();

    if(result != ref){
        printf("ERROR: result = %d, ref = %d", result, ref);
        return 1;
    } else {
        printf("SUCCESS: result = %d, ref = %d", result, ref);
        return 0;
    }

}