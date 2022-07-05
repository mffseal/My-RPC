package top.mffseal.test;

import top.mffseal.rpc.annotation.Service;
import top.mffseal.rpc.api.ByeService;

/**
 * @author mffseal
 */
@Service
public class ByeServiceImpl implements ByeService {
    @Override
    public String bye(String name) {
        return "bye, " + name;
    }
}
