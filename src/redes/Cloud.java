/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package redes;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;
import org.cloudbus.cloudsim.Cloudlet;
import org.cloudbus.cloudsim.CloudletSchedulerTimeShared;
import org.cloudbus.cloudsim.Datacenter;
import org.cloudbus.cloudsim.DatacenterBroker;
import org.cloudbus.cloudsim.DatacenterCharacteristics;
import org.cloudbus.cloudsim.Host;
import org.cloudbus.cloudsim.Pe;
import org.cloudbus.cloudsim.Storage;
import org.cloudbus.cloudsim.UtilizationModelFull;
import org.cloudbus.cloudsim.UtilizationModelStochastic;
import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.VmAllocationPolicySimple;
import org.cloudbus.cloudsim.VmSchedulerTimeShared;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.provisioners.BwProvisionerSimple;
import org.cloudbus.cloudsim.provisioners.PeProvisionerSimple;
import org.cloudbus.cloudsim.provisioners.RamProvisionerSimple;

/**
 *
 * @author Gabriel Alves
 */

public class Cloud {
    public static final int GB = 1024;
    
    public static void main(String[] args) {
        try{
            int numUsers = 1;
            var calendar = Calendar.getInstance();
            boolean traceFlag = false;
            //inicializando o cloudsim
            CloudSim.init(numUsers, calendar, traceFlag);
            
            
            //Criando um datacenter:
            var datacenter = createDatacenter("Meu_Datacenter");
            
            //Criar um broker
            var broker = new DatacenterBroker("Meu_Broker");
            int brokerID = broker.getId();
            
            //Criar as VMs
            
            List<Vm> vmList = new ArrayList<>();
            vmList.add(
                // Criando uma nova máquina virtual (VM)
                new Vm(
                    0,                     // ID da VM (identificação única)
                    brokerID,              // ID do Broker que gerencia esta VM
                    2900,                  // MIPS (Milhões de Instruções por Segundo) -> Capacidade de processamento da VM
                    2,                     // Número de CPUs (Processing Elements - PEs) -> Quantidade de núcleos disponíveis
                    1*GB,                   // Quantidade de memória RAM (MB)
                    1000,                  // Tamanho da imagem do disco da VM (MB) -> Espaço de armazenamento para o SO
                    10000,                 // Largura de banda (BW - Bandwidth) -> Taxa de transferência de rede da VM (Mbps)
                    "Xen",                 // Nome do hypervisor que gerencia a VM (Xen, KVM, VMware, etc.)
                    new CloudletSchedulerTimeShared() // Política de escalonamento das Cloudlets dentro da VM
                )
            );
            
            vmList.add(
                // Criando uma nova máquina virtual (VM)
                new Vm(
                    1,                     // ID da VM (identificação única)
                    brokerID,              // ID do Broker que gerencia esta VM
                    1900,                  // MIPS (Milhões de Instruções por Segundo) -> Capacidade de processamento da VM
                    2,                     // Número de CPUs (Processing Elements - PEs) -> Quantidade de núcleos disponíveis
                    2*GB,                   // Quantidade de memória RAM (MB)
                    1000,                  // Tamanho da imagem do disco da VM (MB) -> Espaço de armazenamento para o SO
                    10000,                 // Largura de banda (BW - Bandwidth) -> Taxa de transferência de rede da VM (Mbps)
                    "Xen",                 // Nome do hypervisor que gerencia a VM (Xen, KVM, VMware, etc.)
                    new CloudletSchedulerTimeShared() // Política de escalonamento das Cloudlets dentro da VM
                )
            );
            
            
            broker.submitGuestList(vmList);
            
            /*
            //Criar e configurar as tarefas
            List<Cloudlet> cloudletList = new ArrayList<>();
            var cloudlet = new Cloudlet(0, 400000, 1, 300, 300, new UtilizationModelFull(), new UtilizationModelFull(), new UtilizationModelFull());
            cloudlet.setUserId(brokerID);
            cloudlet.setGuestId(0);
            cloudletList.add(cloudlet);
            */
            broker.submitCloudletList(criarCloudlets(brokerID));
            
            //Iniciando a simulação
            CloudSim.startSimulation();
            List<Cloudlet> newList = broker.getCloudletReceivedList();
            CloudSim.stopSimulation();
            
            //Exibir resultados.
            printCloudletList(newList);
            
        }catch(Exception e){
            e.printStackTrace();
        }
    }
    
    public static Datacenter createDatacenter(String nome){
        //Lista de hosts dentro do datacenter
        List<Host> hostList = new ArrayList<>();
        //Lista de CPU máquina 1
        List<Pe> peList0 = new ArrayList<>();
        peList0.add(new Pe(0, new PeProvisionerSimple(2000)));
        peList0.add(new Pe(1, new PeProvisionerSimple(2000)));
        //Lista de CPU máquina 2
        List<Pe> peList1 = new ArrayList<>();
        peList1.add(new Pe(0, new PeProvisionerSimple(3000)));
        peList1.add(new Pe(1, new PeProvisionerSimple(3000)));
        
        //Criando as máquinas
        //Máquina 0
        hostList.add(
            new Host(
                    0, //id do host
                    new RamProvisionerSimple(4*GB),  // memória do host em MB
                    new BwProvisionerSimple(20000), //largura de banda
                    1000000, // armazenamento do host
                    peList0,
                    new VmSchedulerTimeShared(peList0)
            )
        );
        //Máquina 1
        hostList.add(
            new Host(
                    1, //id do host
                    new RamProvisionerSimple(3*GB),  // memória do host em MB
                    new BwProvisionerSimple(30000), //largura de banda
                    1000000, // armazenamento do host
                    peList1,
                    new VmSchedulerTimeShared(peList1)
            )
        );
        
        String arch = "x86";
        String os = "Linux";
        String vmm = "Xen";
        double time_zone = 10.0;
        double cost = 3.0;
        double costPerMem = 0.05;
        double costPerStorage = 0.1;
        double costPerBw = 0.1;
        
        LinkedList<Storage> storageList = new LinkedList<>();
        
        var characteristics = new DatacenterCharacteristics(arch, os, vmm, hostList, time_zone, cost, costPerMem, costPerStorage, costPerBw);
        
        Datacenter datacenter = null;
        try{
            datacenter = new Datacenter(nome, characteristics, new VmAllocationPolicySimple(hostList), storageList, 0);
        }catch(Exception e){
            e.printStackTrace();
        }
        return datacenter;
    }
    
    private static void printCloudletList(List<Cloudlet> list) {
        for (Cloudlet cloudlet : list) {
            System.out.println("Cloudlet ID: " + cloudlet.getCloudletId() + " Status: " + cloudlet.getStatus());
        }
    }
    
    private static List<Cloudlet> criarCloudlets(int brokerID){
        // Criando uma lista para armazenar as Cloudlets
        List<Cloudlet> cloudletList = new ArrayList<>();

        // Cloudlet 1: Muito longa, processamento intenso
        Cloudlet cloudlet1 = new Cloudlet(
            0,        // ID
            1_000_000, // Comprimento (número de instruções a serem executadas)
            2,        // Número de CPUs (mais núcleos = mais paralelismo)
            500,      // Tamanho de entrada (em bytes)
            500,      // Tamanho de saída (em bytes)
            new UtilizationModelFull(), 
            new UtilizationModelFull(), 
            new UtilizationModelFull()
        );
        cloudlet1.setUserId(brokerID);
        cloudletList.add(cloudlet1);

        // Cloudlet 2: Menos instruções, mas usa muitos dados (I/O pesado)
        Cloudlet cloudlet2 = new Cloudlet(
            1,
            500_000,  // Metade das instruções do cloudlet 1
            1,        // Apenas um núcleo
            50_000,   // Entrada de dados bem maior
            50_000,   // Saída de dados bem maior
            new UtilizationModelFull(),
            new UtilizationModelFull(),
            new UtilizationModelFull()
        );
        cloudlet2.setUserId(brokerID);
        cloudletList.add(cloudlet2);

        // Cloudlet 3: Pequena, mas usa muitos núcleos (paralelizável)
        Cloudlet cloudlet3 = new Cloudlet(
            2,
            200_000,  // Poucas instruções
            4,        // Usa 4 núcleos para rodar mais rápido
            1_000,    // Pouca entrada de dados
            1_000,    // Pouca saída de dados
            new UtilizationModelFull(),
            new UtilizationModelFull(),
            new UtilizationModelFull()
        );
        cloudlet3.setUserId(brokerID);
        cloudletList.add(cloudlet3);

        // Cloudlet 4: Equilibrada, mas com picos de consumo de CPU
        Cloudlet cloudlet4 = new Cloudlet(
            3,
            700_000,  // Meio termo em quantidade de instruções
            2,        // Usa 2 CPUs
            5_000,    // Tamanho médio de entrada
            5_000,    // Tamanho médio de saída
            new UtilizationModelStochastic(), // Utilização variável, simulando carga instável
            new UtilizationModelFull(),
            new UtilizationModelFull()
        );
        cloudlet4.setUserId(brokerID);
        cloudletList.add(cloudlet4);

        // Enviando as Cloudlets para o Broker processar
        return cloudletList;

    }
}
