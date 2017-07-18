package ca.projecthermes.projecthermes.Ethereum;

import io.ethmobile.ethdroid.solidity.ContractType;
import io.ethmobile.ethdroid.solidity.element.function.SolidityFunction;
import io.ethmobile.ethdroid.solidity.types.SBytes;

/**
 * Created by abc on 2017-07-17.
 */

public interface SmartContract extends ContractType {
    SolidityFunction newMessage(SBytes msg, SBytes pubNonce, SBytes privNonce);
}
